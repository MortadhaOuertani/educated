package tn.tekup.educated.administration.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.administration.domain.Role;
import tn.tekup.educated.administration.domain.RoleRepository;
import tn.tekup.educated.administration.domain.UserAccount;
import tn.tekup.educated.administration.domain.UserRole;
import tn.tekup.educated.administration.domain.UserRoleRepository;
import tn.tekup.educated.shared.web.NotFoundException;

/** Gives roles to users in the current establishment. */
@Service
@Transactional
public class RoleAssignments {

    private final UserRoleRepository userRoles;
    private final RoleRepository roles;

    public RoleAssignments(UserRoleRepository userRoles, RoleRepository roles) {
        this.userRoles = userRoles;
        this.roles = roles;
    }

    public void assign(UserAccount account, Role role) {
        if (!userRoles.existsByUserAccount_IdAndRole_Id(account.getId(), role.getId())) {
            UserRole userRole = new UserRole();
            userRole.setUserAccount(account);
            userRole.setRole(role);
            userRoles.save(userRole);
        }
    }

    public void assign(UserAccount account, String roleName) {
        assign(account, roles.findByName(roleName).orElseThrow(() -> new NotFoundException("Role", roleName)));
    }

    public void remove(UUID userAccountId, UUID roleId) {
        userRoles.findAllByUserAccount_Id(userAccountId).stream()
                .filter(userRole -> userRole.getRole().getId().equals(roleId))
                .forEach(userRoles::delete);
    }

    /** Roles of the user in the current establishment that are valid today. */
    @Transactional(readOnly = true)
    public List<Role> activeRoles(UUID userAccountId) {
        LocalDate today = LocalDate.now();
        return userRoles.findAllByUserAccount_Id(userAccountId).stream()
                .filter(userRole -> isValidOn(userRole, today))
                .map(UserRole::getRole)
                .toList();
    }

    public static boolean isValidOn(UserRole userRole, LocalDate day) {
        return (userRole.getValidFrom() == null || !userRole.getValidFrom().isAfter(day))
                && (userRole.getValidTo() == null || !userRole.getValidTo().isBefore(day));
    }
}
