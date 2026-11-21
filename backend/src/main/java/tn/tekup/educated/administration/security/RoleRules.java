package tn.tekup.educated.administration.security;

import java.util.Collection;
import tn.tekup.educated.administration.domain.DeniedAction;
import tn.tekup.educated.administration.domain.NavigationPermission;
import tn.tekup.educated.administration.domain.PermissionPolicy;
import tn.tekup.educated.administration.domain.PermissionState;
import tn.tekup.educated.administration.domain.Role;
import tn.tekup.educated.administration.domain.TypePermission;
import tn.tekup.educated.shared.domain.AppModule;
import tn.tekup.educated.shared.security.Operation;

/**
 * The rules of the role editor. A user with several roles gets an action as soon as one role allows it.
 * An administrative role allows everything.
 */
final class RoleRules {

    private RoleRules() {
    }

    static boolean can(Collection<Role> roles, String targetType, Operation operation) {
        return roles.stream().anyMatch(role -> can(role, targetType, operation));
    }

    static boolean can(Role role, String targetType, Operation operation) {
        if (role.isAdministrative()) {
            return true;
        }
        for (TypePermission permission : role.getTypePermissions()) {
            if (permission.getTargetType().equals(targetType)) {
                PermissionState state = stateFor(permission, operation);
                if (state == PermissionState.ALLOW) {
                    return true;
                }
                if (state == PermissionState.DENY) {
                    return false;
                }
            }
        }
        return policyAllows(role.getPermissionPolicy(), operation);
    }

    static boolean canNavigate(Collection<Role> roles, String item) {
        return roles.stream().anyMatch(role -> canNavigate(role, item));
    }

    static boolean canNavigate(Role role, String item) {
        if (role.isAdministrative()) {
            return true;
        }
        for (NavigationPermission permission : role.getNavigationPermissions()) {
            if (permission.getItemPath().equals(item) && permission.getCanNavigate() != PermissionState.INHERIT) {
                return permission.getCanNavigate() == PermissionState.ALLOW;
            }
        }
        return role.getPermissionPolicy() != PermissionPolicy.DENY_ALL_BY_DEFAULT;
    }

    static boolean canRunAction(Collection<Role> roles, String actionKey) {
        return roles.stream().anyMatch(role -> role.isAdministrative()
                || role.getDeniedActions().stream().map(DeniedAction::getActionKey).noneMatch(actionKey::equals));
    }

    static boolean isModuleVisible(Collection<Role> roles, AppModule module) {
        return roles.stream().anyMatch(role -> role.isAdministrative()
                || role.getModuleAccess().stream().anyMatch(a -> a.getModule() == module && a.isVisible()));
    }

    private static boolean policyAllows(PermissionPolicy policy, Operation operation) {
        return switch (policy) {
            case ALLOW_ALL_BY_DEFAULT -> true;
            case READ_ONLY_ALL_BY_DEFAULT -> operation == Operation.READ || operation == Operation.NAVIGATE;
            case DENY_ALL_BY_DEFAULT -> false;
        };
    }

    private static PermissionState stateFor(TypePermission permission, Operation operation) {
        return switch (operation) {
            case READ -> permission.getCanRead();
            case WRITE -> permission.getCanWrite();
            case CREATE -> permission.getCanCreate();
            case DELETE -> permission.getCanDelete();
            case NAVIGATE -> permission.getCanNavigate();
        };
    }
}
