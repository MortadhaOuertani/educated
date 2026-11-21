package tn.tekup.educated.administration.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import tn.tekup.educated.administration.domain.ApprovalStatus;
import tn.tekup.educated.administration.domain.DeniedAction;
import tn.tekup.educated.administration.domain.DisplayDensity;
import tn.tekup.educated.administration.domain.PermissionPolicy;
import tn.tekup.educated.administration.domain.PermissionState;
import tn.tekup.educated.administration.domain.Role;
import tn.tekup.educated.administration.domain.RoleChangeRequest;
import tn.tekup.educated.administration.domain.RoleChangeType;
import tn.tekup.educated.administration.domain.RoleModuleAccess;
import tn.tekup.educated.administration.domain.RolePreset;
import tn.tekup.educated.administration.domain.ThemeMode;
import tn.tekup.educated.administration.domain.UserAccount;
import tn.tekup.educated.shared.domain.AppModule;
import tn.tekup.educated.shared.domain.PersonType;
import tn.tekup.educated.shared.security.Operation;

/** Request and response bodies of the administration REST API. */
final class AdministrationDtos {

    private AdministrationDtos() {
    }

    record MeDto(UUID userId, String username, String email, String firstName, String lastName, PersonType personType,
            UUID personId, boolean administrative, ThemeMode theme, DisplayDensity displayDensity,
            String preferredLocale, UUID establishmentId, List<AppModule> visibleModules,
            java.util.Map<String, List<Operation>> permissions, List<String> deniedActions) {
    }

    record PreferencesRequest(@NotNull ThemeMode theme, @NotNull DisplayDensity displayDensity,
            @NotBlank @Size(max = 5) String preferredLocale) {
    }

    record RoleSummaryDto(UUID id, String name, String description, boolean administrative, boolean canEditModel,
            PermissionPolicy permissionPolicy, RolePreset preset, boolean systemRole) {
        static RoleSummaryDto from(Role r) {
            return new RoleSummaryDto(r.getId(), r.getName(), r.getDescription(), r.isAdministrative(),
                    r.isCanEditModel(), r.getPermissionPolicy(), r.getPreset(), r.isSystemRole());
        }
    }

    record TypeRuleDto(@NotBlank String targetType, PermissionState read, PermissionState write,
            PermissionState create, PermissionState delete, PermissionState navigate) {
    }

    record NavigationRuleDto(@NotBlank String itemPath, PermissionState navigate) {
    }

    record RoleDetailDto(RoleSummaryDto role, List<AppModule> visibleModules, List<TypeRuleDto> typePermissions,
            List<NavigationRuleDto> navigationPermissions, List<String> deniedActions) {
        static RoleDetailDto from(Role r) {
            return new RoleDetailDto(RoleSummaryDto.from(r),
                    r.getModuleAccess().stream().filter(RoleModuleAccess::isVisible).map(RoleModuleAccess::getModule).toList(),
                    r.getTypePermissions().stream().map(p -> new TypeRuleDto(p.getTargetType(), p.getCanRead(),
                            p.getCanWrite(), p.getCanCreate(), p.getCanDelete(), p.getCanNavigate())).toList(),
                    r.getNavigationPermissions().stream()
                            .map(p -> new NavigationRuleDto(p.getItemPath(), p.getCanNavigate())).toList(),
                    r.getDeniedActions().stream().map(DeniedAction::getActionKey).toList());
        }
    }

    record NewRoleRequest(@NotBlank @Size(max = 100) String name, @Size(max = 500) String description,
            PermissionPolicy permissionPolicy) {
    }

    record RoleLayersRequest(@Size(max = 500) String description, @NotNull List<AppModule> visibleModules,
            @NotNull List<TypeRuleDto> typePermissions, @NotNull List<NavigationRuleDto> navigationPermissions,
            @NotNull List<String> deniedActions) {
    }

    record ChangeRequestBody(@NotNull RoleChangeType changeType, PermissionPolicy permissionPolicy,
            Boolean administrative, Boolean canEditModel) {
    }

    record ChangeRequestDto(UUID id, UUID roleId, String roleName, RoleChangeType changeType, String payloadJson,
            ApprovalStatus status, UUID requestedByUserId, Instant requestedAt, UUID decidedByUserId,
            Instant decidedAt) {
        static ChangeRequestDto from(RoleChangeRequest r) {
            return new ChangeRequestDto(r.getId(), r.getRole().getId(), r.getRole().getName(), r.getChangeType(),
                    r.getPayloadJson(), r.getStatus(), r.getCreatedByUserId(), r.getCreatedAt(),
                    r.getDecidedByUserId(), r.getDecidedAt());
        }
    }

    record UserDto(UUID id, String username, String email, boolean active, PersonType personType, UUID personId,
            Instant lastLoginAt) {
        static UserDto from(UserAccount u) {
            return new UserDto(u.getId(), u.getUsername(), u.getEmail(), u.isActive(), u.getPersonType(),
                    u.getPersonId(), u.getLastLoginAt());
        }
    }

    record UserDetailDto(UserDto user, List<RoleSummaryDto> roles) {
    }

    record AssignRoleRequest(@NotNull UUID roleId) {
    }

    record ActiveRequest(boolean active) {
    }
}
