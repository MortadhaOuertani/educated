package tn.tekup.educated.administration.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import tn.tekup.educated.administration.domain.DeniedAction;
import tn.tekup.educated.administration.domain.NavigationPermission;
import tn.tekup.educated.administration.domain.PermissionPolicy;
import tn.tekup.educated.administration.domain.PermissionState;
import tn.tekup.educated.administration.domain.Role;
import tn.tekup.educated.administration.domain.RolePreset;
import tn.tekup.educated.administration.domain.TypePermission;
import tn.tekup.educated.shared.domain.AppModule;
import tn.tekup.educated.shared.security.Operation;

class RoleRulesTest {

    @Test
    void administrativeRoleAllowsEverything() {
        Role admin = role(PermissionPolicy.DENY_ALL_BY_DEFAULT);
        admin.setAdministrative(true);

        assertThat(RoleRules.can(admin, "Student", Operation.DELETE)).isTrue();
        assertThat(RoleRules.canNavigate(admin, "Administration")).isTrue();
        assertThat(RoleRules.isModuleVisible(List.of(admin), AppModule.FINANCES)).isTrue();
    }

    @Test
    void policyIsUsedWhenNoTypePermissionMatches() {
        assertThat(RoleRules.can(role(PermissionPolicy.DENY_ALL_BY_DEFAULT), "Student", Operation.READ)).isFalse();
        assertThat(RoleRules.can(role(PermissionPolicy.READ_ONLY_ALL_BY_DEFAULT), "Student", Operation.READ)).isTrue();
        assertThat(RoleRules.can(role(PermissionPolicy.READ_ONLY_ALL_BY_DEFAULT), "Student", Operation.WRITE)).isFalse();
        assertThat(RoleRules.can(role(PermissionPolicy.ALLOW_ALL_BY_DEFAULT), "Student", Operation.DELETE)).isTrue();
    }

    @Test
    void typePermissionOverridesThePolicy() {
        // Like the survey's "Scolarité" role: allow all, but delete denied on Apprenant.
        Role scolarite = role(PermissionPolicy.ALLOW_ALL_BY_DEFAULT);
        scolarite.getTypePermissions().add(permission(scolarite, "Student", PermissionState.INHERIT, PermissionState.DENY));

        assertThat(RoleRules.can(scolarite, "Student", Operation.READ)).isTrue();
        assertThat(RoleRules.can(scolarite, "Student", Operation.DELETE)).isFalse();
        assertThat(RoleRules.can(scolarite, "Guardian", Operation.DELETE)).isTrue();
    }

    @Test
    void oneAllowingRoleIsEnough() {
        Role denyAll = role(PermissionPolicy.DENY_ALL_BY_DEFAULT);
        Role readStudents = role(PermissionPolicy.DENY_ALL_BY_DEFAULT);
        readStudents.getTypePermissions().add(permission(readStudents, "Student", PermissionState.ALLOW, PermissionState.INHERIT));

        assertThat(RoleRules.can(List.of(denyAll, readStudents), "Student", Operation.READ)).isTrue();
        assertThat(RoleRules.can(List.of(denyAll, readStudents), "Student", Operation.DELETE)).isFalse();
    }

    @Test
    void navigationAndDeniedActions() {
        Role role = role(PermissionPolicy.ALLOW_ALL_BY_DEFAULT);
        NavigationPermission administrationMenu = new NavigationPermission();
        administrationMenu.setRole(role);
        administrationMenu.setItemPath("Administration");
        administrationMenu.setCanNavigate(PermissionState.DENY);
        role.getNavigationPermissions().add(administrationMenu);
        DeniedAction pay = new DeniedAction();
        pay.setRole(role);
        pay.setActionKey("Payer");
        role.getDeniedActions().add(pay);

        assertThat(RoleRules.canNavigate(role, "Administration")).isFalse();
        assertThat(RoleRules.canNavigate(role, "Scolarite/Students")).isTrue();
        assertThat(RoleRules.canRunAction(List.of(role), "Payer")).isFalse();
        assertThat(RoleRules.canRunAction(List.of(role), "Publier")).isTrue();
        assertThat(RoleRules.isModuleVisible(List.of(role), AppModule.FINANCES)).isFalse();
    }

    private static Role role(PermissionPolicy policy) {
        Role role = new Role();
        role.setName("Test");
        role.setPermissionPolicy(policy);
        role.setPreset(RolePreset.CUSTOM);
        return role;
    }

    private static TypePermission permission(Role role, String type, PermissionState read, PermissionState delete) {
        TypePermission permission = new TypePermission();
        permission.setRole(role);
        permission.setTargetType(type);
        permission.setCanRead(read);
        permission.setCanWrite(PermissionState.INHERIT);
        permission.setCanCreate(PermissionState.INHERIT);
        permission.setCanDelete(delete);
        permission.setCanNavigate(PermissionState.INHERIT);
        return permission;
    }
}
