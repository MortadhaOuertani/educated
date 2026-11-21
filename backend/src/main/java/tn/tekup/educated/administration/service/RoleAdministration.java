package tn.tekup.educated.administration.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.administration.api.RoleChanged;
import tn.tekup.educated.administration.domain.ApprovalStatus;
import tn.tekup.educated.administration.domain.DeniedAction;
import tn.tekup.educated.administration.domain.NavigationPermission;
import tn.tekup.educated.administration.domain.PermissionPolicy;
import tn.tekup.educated.administration.domain.PermissionState;
import tn.tekup.educated.administration.domain.Role;
import tn.tekup.educated.administration.domain.RoleChangeRequest;
import tn.tekup.educated.administration.domain.RoleChangeRequestRepository;
import tn.tekup.educated.administration.domain.RoleChangeType;
import tn.tekup.educated.administration.domain.RoleModuleAccess;
import tn.tekup.educated.administration.domain.RolePreset;
import tn.tekup.educated.administration.domain.RoleRepository;
import tn.tekup.educated.administration.domain.TypePermission;
import tn.tekup.educated.administration.domain.UserAccount;
import tn.tekup.educated.administration.domain.UserAccountRepository;
import tn.tekup.educated.shared.audit.AuditLogger;
import tn.tekup.educated.shared.domain.AppModule;
import tn.tekup.educated.shared.security.CurrentUser;
import tn.tekup.educated.shared.security.CurrentUserProvider;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Role and user administration. Two safety rules from the survey findings:
 * nobody changes roles they hold themselves, and admin-level changes need a second Super Admin.
 */
@Service
@Transactional
public class RoleAdministration {

    public record TypeRule(String targetType, PermissionState read, PermissionState write, PermissionState create,
            PermissionState delete, PermissionState navigate) {
    }

    public record NavigationRule(String itemPath, PermissionState navigate) {
    }

    /** What a role change request asks for (stored as JSON in RoleChangeRequest.payloadJson). */
    public record ChangePayload(UUID userAccountId, PermissionPolicy policy, Boolean administrative,
            Boolean canEditModel) {
    }

    private final RoleRepository roles;
    private final UserAccountRepository accounts;
    private final RoleChangeRequestRepository changeRequests;
    private final RoleAssignments roleAssignments;
    private final CurrentUserProvider currentUserProvider;
    private final AuditLogger audit;
    private final ApplicationEventPublisher events;
    private final JsonMapper json;

    public RoleAdministration(RoleRepository roles, UserAccountRepository accounts,
            RoleChangeRequestRepository changeRequests, RoleAssignments roleAssignments,
            CurrentUserProvider currentUserProvider, AuditLogger audit, ApplicationEventPublisher events,
            JsonMapper json) {
        this.roles = roles;
        this.accounts = accounts;
        this.changeRequests = changeRequests;
        this.roleAssignments = roleAssignments;
        this.currentUserProvider = currentUserProvider;
        this.audit = audit;
        this.events = events;
        this.json = json;
    }

    @Transactional(readOnly = true)
    public List<Role> roles() {
        return roles.findAllByOrderByName();
    }

    @Transactional(readOnly = true)
    public Role role(UUID id) {
        return roles.findById(id).orElseThrow(() -> new NotFoundException("Role", id));
    }

    public Role createRole(String name, String description, PermissionPolicy policy) {
        if (roles.findByName(name).isPresent()) {
            throw new BusinessRuleException("role.duplicate", "A role named " + name + " already exists.");
        }
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        role.setPreset(RolePreset.CUSTOM);
        role.setPermissionPolicy(policy == null ? PermissionPolicy.DENY_ALL_BY_DEFAULT : policy);
        if (role.getPermissionPolicy() == PermissionPolicy.ALLOW_ALL_BY_DEFAULT) {
            throw new BusinessRuleException("role.policy",
                    "New roles start from 'deny all' or 'read only'; 'allow all' needs an approved change.");
        }
        Role saved = roles.save(role);
        audit.record("administration", "ROLE_CREATED", "Role", saved.getId(), null);
        return saved;
    }

    /** Replaces the module, type, navigation and denied-action layers of a role. */
    public Role updateLayers(UUID roleId, String description, List<AppModule> visibleModules, List<TypeRule> typeRules,
            List<NavigationRule> navigationRules, List<String> deniedActions) {
        Role role = editableRole(roleId);
        role.setDescription(description);

        role.getModuleAccess().clear();
        for (AppModule module : visibleModules) {
            RoleModuleAccess access = new RoleModuleAccess();
            access.setRole(role);
            access.setModule(module);
            access.setVisible(true);
            role.getModuleAccess().add(access);
        }
        role.getTypePermissions().clear();
        for (TypeRule rule : typeRules) {
            TypePermission permission = new TypePermission();
            permission.setRole(role);
            permission.setTargetType(rule.targetType());
            permission.setCanRead(orInherit(rule.read()));
            permission.setCanWrite(orInherit(rule.write()));
            permission.setCanCreate(orInherit(rule.create()));
            permission.setCanDelete(orInherit(rule.delete()));
            permission.setCanNavigate(orInherit(rule.navigate()));
            role.getTypePermissions().add(permission);
        }
        role.getNavigationPermissions().clear();
        for (NavigationRule rule : navigationRules) {
            NavigationPermission permission = new NavigationPermission();
            permission.setRole(role);
            permission.setItemPath(rule.itemPath());
            permission.setCanNavigate(orInherit(rule.navigate()));
            role.getNavigationPermissions().add(permission);
        }
        role.getDeniedActions().clear();
        for (String actionKey : deniedActions) {
            DeniedAction denied = new DeniedAction();
            denied.setRole(role);
            denied.setActionKey(actionKey);
            role.getDeniedActions().add(denied);
        }
        roles.flush();
        audit.record("administration", "ROLE_LAYERS_UPDATED", "Role", role.getId(), null);
        events.publishEvent(new RoleChanged(role.getEstablishmentId(), role.getId()));
        return role;
    }

    /** Changing the policy or the admin flags is only a request until another Super Admin approves it. */
    public RoleChangeRequest requestChange(UUID roleId, RoleChangeType type, ChangePayload payload) {
        Role role = type == RoleChangeType.ASSIGN_ROLE ? role(roleId) : editableRole(roleId);
        String payloadJson = json.writeValueAsString(payload);
        RoleChangeRequest request = new RoleChangeRequest();
        request.setRole(role);
        request.setChangeType(type);
        request.setPayloadJson(payloadJson);
        request.setStatus(ApprovalStatus.PENDING);
        RoleChangeRequest saved = changeRequests.save(request);
        audit.record("administration", "ROLE_CHANGE_REQUESTED", "RoleChangeRequest", saved.getId(), payloadJson);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<RoleChangeRequest> pendingRequests() {
        return changeRequests.findAllByStatusOrderByCreatedAt(ApprovalStatus.PENDING);
    }

    public RoleChangeRequest decide(UUID requestId, boolean approve) {
        CurrentUser approver = currentUserProvider.require();
        RoleChangeRequest request = changeRequests.findById(requestId)
                .orElseThrow(() -> new NotFoundException("RoleChangeRequest", requestId));
        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessRuleException("roleChange.decided", "This request was already decided.");
        }
        if (!approver.administrative()) {
            throw new BusinessRuleException("roleChange.notAdmin", "Only a Super Admin can decide this request.");
        }
        if (approver.userId().equals(request.getCreatedByUserId())) {
            throw new BusinessRuleException("roleChange.sameUser", "Another Super Admin must approve your own request.");
        }
        if (approve) {
            apply(request);
        }
        request.setStatus(approve ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED);
        request.setDecidedByUserId(approver.userId());
        request.setDecidedAt(Instant.now());
        audit.record("administration", approve ? "ROLE_CHANGE_APPROVED" : "ROLE_CHANGE_REJECTED",
                "RoleChangeRequest", request.getId(), request.getPayloadJson());
        events.publishEvent(new RoleChanged(request.getEstablishmentId(), request.getRole().getId()));
        return request;
    }

    private void apply(RoleChangeRequest request) {
        ChangePayload payload = json.readValue(request.getPayloadJson(), ChangePayload.class);
        Role role = request.getRole();
        switch (request.getChangeType()) {
            case ASSIGN_ROLE -> roleAssignments.assign(user(payload.userAccountId()), role);
            case CHANGE_POLICY -> role.setPermissionPolicy(payload.policy());
            case GRANT_ADMINISTRATIVE -> role.setAdministrative(Boolean.TRUE.equals(payload.administrative()));
            case GRANT_CAN_EDIT_MODEL -> role.setCanEditModel(Boolean.TRUE.equals(payload.canEditModel()));
            default -> throw new BusinessRuleException("roleChange.unsupported",
                    "Unsupported change: " + request.getChangeType());
        }
    }

    @Transactional(readOnly = true)
    public Page<UserAccount> users(String search, Pageable pageable) {
        String text = search == null ? "" : search.trim();
        return accounts.findAllByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(text, text, pageable);
    }

    @Transactional(readOnly = true)
    public UserAccount user(UUID id) {
        return accounts.findById(id).orElseThrow(() -> new NotFoundException("UserAccount", id));
    }

    /** Gives a role. An administrative role becomes a request waiting for approval. Returns null when requested. */
    public RoleChangeRequest assignRole(UUID userId, UUID roleId) {
        rejectSelfChange(userId);
        Role role = role(roleId);
        if (role.isAdministrative() || role.isCanEditModel()) {
            return requestChange(roleId, RoleChangeType.ASSIGN_ROLE, new ChangePayload(userId, null, null, null));
        }
        roleAssignments.assign(user(userId), role);
        audit.record("administration", "ROLE_ASSIGNED", "UserAccount", userId, "{\"roleId\":\"" + roleId + "\"}");
        return null;
    }

    public void removeRole(UUID userId, UUID roleId) {
        rejectSelfChange(userId);
        roleAssignments.remove(userId, roleId);
        audit.record("administration", "ROLE_REMOVED", "UserAccount", userId, "{\"roleId\":\"" + roleId + "\"}");
    }

    public UserAccount setActive(UUID userId, boolean active) {
        rejectSelfChange(userId);
        UserAccount account = user(userId);
        account.setActive(active);
        audit.record("administration", active ? "USER_ACTIVATED" : "USER_DEACTIVATED", "UserAccount", userId, null);
        return account;
    }

    private void rejectSelfChange(UUID userId) {
        if (currentUserProvider.require().userId().equals(userId)) {
            throw new BusinessRuleException("user.selfChange", "You cannot change your own access.");
        }
    }

    /** A user who is not a Super Admin may not edit a role they hold. Super Admin is never edited directly. */
    private Role editableRole(UUID roleId) {
        Role role = role(roleId);
        CurrentUser user = currentUserProvider.require();
        if (role.isAdministrative()) {
            throw new BusinessRuleException("role.protected", "The Super Admin role cannot be edited.");
        }
        boolean holdsIt = roleAssignments.activeRoles(user.userId()).stream().anyMatch(r -> r.getId().equals(roleId));
        if (holdsIt && !user.administrative()) {
            throw new BusinessRuleException("role.selfEdit", "You cannot edit a role you hold.");
        }
        return role;
    }

    private static PermissionState orInherit(PermissionState state) {
        return state == null ? PermissionState.INHERIT : state;
    }
}
