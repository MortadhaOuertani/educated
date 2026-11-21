package tn.tekup.educated.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tn.tekup.educated.administration.api.AccountPreparation;
import tn.tekup.educated.pedagogie.api.TeachingDirectory;
import tn.tekup.educated.scolarite.api.StudentDirectory;
import tn.tekup.educated.shared.domain.PersonType;
import tn.tekup.educated.shared.security.EstablishmentDirectory;
import tn.tekup.educated.shared.tenant.TenantContext;

/**
 * Prepares the accounts of the Keycloak test users (see infra/keycloak/educated-realm.json) so each portal can be
 * tried right away. The super admin is set up automatically on first sign-in.
 */
@Component
@Order(90)
@ConditionalOnBooleanProperty("educated.bootstrap.demo-data")
class DemoAccounts implements ApplicationRunner {

    private final EstablishmentDirectory establishments;
    private final AccountPreparation accounts;
    private final StudentDirectory students;
    private final TeachingDirectory teaching;

    DemoAccounts(EstablishmentDirectory establishments, AccountPreparation accounts, StudentDirectory students,
            TeachingDirectory teaching) {
        this.establishments = establishments;
        this.accounts = accounts;
        this.students = students;
        this.teaching = teaching;
    }

    @Override
    public void run(ApplicationArguments args) {
        establishments.allEstablishmentIds().stream().findFirst().ifPresent(establishmentId ->
                TenantContext.runAs(establishmentId, () -> {
                    accounts.prepare("scolarite.agent", "scolarite@educated.local", PersonType.STAFF, null, "Scolarité");
                    teaching.findTeacherByEmployeeNumber("DEMO-T-001").ifPresent(teacher -> accounts.prepare(
                            "teacher.demo", "teacher@educated.local", PersonType.TEACHER, teacher.id(), "Enseignant"));
                    students.findByMatricule("DEMO-0001").ifPresent(student -> accounts.prepare("student.demo",
                            "student@educated.local", PersonType.STUDENT, student.id(), "Étudiant"));
                }));
    }
}
