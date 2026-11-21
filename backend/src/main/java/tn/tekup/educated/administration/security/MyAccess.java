package tn.tekup.educated.administration.security;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.administration.domain.Role;
import tn.tekup.educated.administration.service.RoleAssignments;
import tn.tekup.educated.shared.domain.AppModule;
import tn.tekup.educated.shared.security.CurrentUser;
import tn.tekup.educated.shared.security.Operation;

/** Everything the front-ends need to show or hide menus and buttons for the current user. */
@Service
@Transactional(readOnly = true)
public class MyAccess {

    public record Summary(List<AppModule> visibleModules, Map<String, List<Operation>> permissions,
            List<String> deniedActions) {
    }

    private final RoleAssignments roleAssignments;
    private final EntityManager entityManager;

    MyAccess(RoleAssignments roleAssignments, EntityManager entityManager) {
        this.roleAssignments = roleAssignments;
        this.entityManager = entityManager;
    }

    public Summary of(CurrentUser user) {
        List<Role> roles = roleAssignments.activeRoles(user.userId());
        List<AppModule> modules = Arrays.stream(AppModule.values())
                .filter(module -> user.administrative() || RoleRules.isModuleVisible(roles, module))
                .toList();
        Map<String, List<Operation>> permissions = new TreeMap<>();
        for (EntityType<?> entity : entityManager.getMetamodel().getEntities()) {
            List<Operation> allowed = Arrays.stream(Operation.values())
                    .filter(op -> user.administrative() || RoleRules.can(roles, entity.getName(), op))
                    .toList();
            if (!allowed.isEmpty()) {
                permissions.put(entity.getName(), allowed);
            }
        }
        List<String> denied = user.administrative() ? List.of() : roles.stream()
                .flatMap(role -> role.getDeniedActions().stream())
                .map(action -> action.getActionKey())
                .filter(key -> !RoleRules.canRunAction(roles, key))
                .distinct()
                .sorted()
                .toList();
        return new Summary(modules, permissions, denied);
    }
}
