package tn.tekup.educated.administration.security;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.administration.domain.Role;
import tn.tekup.educated.administration.service.RoleAssignments;
import tn.tekup.educated.shared.domain.AppModule;
import tn.tekup.educated.shared.security.CurrentUser;
import tn.tekup.educated.shared.security.CurrentUserHolder;
import tn.tekup.educated.shared.security.Operation;
import tn.tekup.educated.shared.security.Permissions;

/** The {@code @perm} bean used in {@code @PreAuthorize}. Uses the user's roles in the current establishment. */
@Component(Permissions.BEAN_NAME)
@Transactional(readOnly = true)
class PermissionService implements Permissions {

    private final RoleAssignments roleAssignments;

    PermissionService(RoleAssignments roleAssignments) {
        this.roleAssignments = roleAssignments;
    }

    @Override
    public boolean can(String targetType, Operation operation) {
        CurrentUser user = CurrentUserHolder.get().orElse(null);
        if (user == null) {
            return false;
        }
        return user.administrative() || RoleRules.can(roles(user), targetType, operation);
    }

    @Override
    public boolean can(String targetType, String operation) {
        return can(targetType, Operation.valueOf(operation));
    }

    @Override
    public boolean canNavigate(String navigationItem) {
        CurrentUser user = CurrentUserHolder.get().orElse(null);
        return user != null && (user.administrative() || RoleRules.canNavigate(roles(user), navigationItem));
    }

    @Override
    public boolean canRunAction(String actionKey) {
        CurrentUser user = CurrentUserHolder.get().orElse(null);
        return user != null && (user.administrative() || RoleRules.canRunAction(roles(user), actionKey));
    }

    @Override
    public boolean isModuleVisible(AppModule module) {
        CurrentUser user = CurrentUserHolder.get().orElse(null);
        return user != null && (user.administrative() || RoleRules.isModuleVisible(roles(user), module));
    }

    private List<Role> roles(CurrentUser user) {
        return roleAssignments.activeRoles(user.userId());
    }
}
