package tn.tekup.educated.administration.setup;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.administration.domain.PermissionPolicy;
import tn.tekup.educated.administration.domain.PermissionState;
import tn.tekup.educated.administration.domain.Role;
import tn.tekup.educated.administration.domain.RoleModuleAccess;
import tn.tekup.educated.administration.domain.RolePreset;
import tn.tekup.educated.administration.domain.RoleRepository;
import tn.tekup.educated.administration.domain.TypePermission;
import tn.tekup.educated.shared.domain.AppModule;

/**
 * Roles every establishment starts with. The Scolarité role follows the recommended baseline
 * (deny by default, explicit grants) instead of "allow all by default".
 */
@Component
public class SystemRoles {

    public static final String SUPER_ADMIN = "Super Admin";
    public static final String SCOLARITE = "Scolarité";
    public static final String TEACHER = "Enseignant";
    public static final String STUDENT = "Étudiant";
    public static final String GUARDIAN = "Responsable";

    private static final List<String> SCOLARITE_MANAGED = List.of("Student", "Guardian", "StudentGuardian", "Kinship",
            "SiblingRelation", "Enrollment", "ClassAssignment", "Sanction", "DocumentRequest", "PreRegistrationRequest",
            "StudentCard", "AttendanceSheet", "AttendanceRecord", "StudentAbsenceRequest", "Message");
    private static final List<String> SCOLARITE_READ_ONLY = List.of("DocumentType", "AcademicYear", "AcademicPeriod",
            "StudyLevel", "SchoolClass", "Program", "Cycle", "Specialization", "SessionOccurrence", "Timetable",
            "AttendancePolicy");

    private final RoleRepository roles;

    SystemRoles(RoleRepository roles) {
        this.roles = roles;
    }

    /** Creates the missing system roles in the current establishment. */
    @Transactional
    public void ensureExist() {
        if (roles.findByName(SUPER_ADMIN).isEmpty()) {
            Role superAdmin = role(SUPER_ADMIN, "Accès complet, crée les autres rôles.", RolePreset.CUSTOM,
                    PermissionPolicy.ALLOW_ALL_BY_DEFAULT);
            superAdmin.setAdministrative(true);
            superAdmin.setCanEditModel(true);
            roles.save(superAdmin);
        }
        if (roles.findByName(SCOLARITE).isEmpty()) {
            Role scolarite = role(SCOLARITE, "Dossiers des apprenants, inscriptions, assiduité, documents.",
                    RolePreset.SCOLARITE, PermissionPolicy.DENY_ALL_BY_DEFAULT);
            SCOLARITE_MANAGED.forEach(type -> scolarite.getTypePermissions().add(permission(scolarite, type,
                    PermissionState.ALLOW, PermissionState.ALLOW, PermissionState.ALLOW, PermissionState.DENY)));
            SCOLARITE_READ_ONLY.forEach(type -> scolarite.getTypePermissions().add(permission(scolarite, type,
                    PermissionState.ALLOW, PermissionState.DENY, PermissionState.DENY, PermissionState.DENY)));
            scolarite.getModuleAccess().add(visible(scolarite, AppModule.SCOLARITE));
            scolarite.getModuleAccess().add(visible(scolarite, AppModule.COMMUNICATION));
            roles.save(scolarite);
        }
        createPortalRole(TEACHER, "Accès à l'espace enseignant.", RolePreset.TEACHER);
        createPortalRole(STUDENT, "Accès à l'espace étudiant.", RolePreset.STUDENT);
        createPortalRole(GUARDIAN, "Accès des parents à l'application mobile.", RolePreset.GUARDIAN);
    }

    /** Portal users only use their own data through the self-service endpoints, so they get no back-office rights. */
    private void createPortalRole(String name, String description, RolePreset preset) {
        if (roles.findByName(name).isEmpty()) {
            roles.save(role(name, description, preset, PermissionPolicy.DENY_ALL_BY_DEFAULT));
        }
    }

    private static Role role(String name, String description, RolePreset preset, PermissionPolicy policy) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        role.setPreset(preset);
        role.setPermissionPolicy(policy);
        role.setSystemRole(true);
        return role;
    }

    private static TypePermission permission(Role role, String type, PermissionState read, PermissionState write,
            PermissionState create, PermissionState delete) {
        TypePermission permission = new TypePermission();
        permission.setRole(role);
        permission.setTargetType(type);
        permission.setCanRead(read);
        permission.setCanWrite(write);
        permission.setCanCreate(create);
        permission.setCanDelete(delete);
        permission.setCanNavigate(read);
        return permission;
    }

    private static RoleModuleAccess visible(Role role, AppModule module) {
        RoleModuleAccess access = new RoleModuleAccess();
        access.setRole(role);
        access.setModule(module);
        access.setVisible(true);
        return access;
    }
}
