package tn.tekup.educated.scolarite.setup;

import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tn.tekup.educated.parametrage.api.ParametrageDirectory;
import tn.tekup.educated.scolarite.domain.DocumentLanguage;
import tn.tekup.educated.scolarite.domain.DocumentType;
import tn.tekup.educated.scolarite.domain.DocumentTypeRepository;
import tn.tekup.educated.scolarite.domain.EnrollmentType;
import tn.tekup.educated.scolarite.domain.Kinship;
import tn.tekup.educated.scolarite.domain.KinshipRepository;
import tn.tekup.educated.scolarite.domain.StudentRepository;
import tn.tekup.educated.scolarite.service.EnrollmentService;
import tn.tekup.educated.scolarite.service.StudentService;
import tn.tekup.educated.scolarite.service.StudentService.StudentData;
import tn.tekup.educated.shared.EducatedProperties;
import tn.tekup.educated.shared.domain.Gender;
import tn.tekup.educated.shared.domain.LocalizedText;
import tn.tekup.educated.shared.domain.PersonInfo;
import tn.tekup.educated.shared.security.EstablishmentDirectory;
import tn.tekup.educated.shared.tenant.TenantContext;

/** Reference data every establishment needs (relationships, document catalogue) and, in development, a demo student. */
@Component
@Order(30)
class ScolariteBootstrap implements ApplicationRunner {

    static final String DEMO_MATRICULE = "DEMO-0001";

    private record Doc(String code, DocumentLanguage language, String label) {
    }

    /** The 11 documents students can request in the current portal. */
    private static final List<Doc> DOCUMENTS = List.of(
            new Doc("ATTESTATION_INSCRIPTION", DocumentLanguage.EN, "Attestation d'inscription (anglais)"),
            new Doc("ATTESTATION_INSCRIPTION", DocumentLanguage.AR, "Attestation d'inscription (arabe)"),
            new Doc("ATTESTATION_INSCRIPTION", DocumentLanguage.FR, "Attestation d'inscription (français)"),
            new Doc("ATTESTATION_DIPLOME", DocumentLanguage.FR, "Attestation de diplôme"),
            new Doc("ATTESTATION_PAIEMENT", DocumentLanguage.EN, "Attestation de paiement (anglais)"),
            new Doc("ATTESTATION_PAIEMENT", DocumentLanguage.FR, "Attestation de paiement (français)"),
            new Doc("ATTESTATION_PRESENCE", DocumentLanguage.EN, "Attestation de présence (anglais)"),
            new Doc("ATTESTATION_PRESENCE", DocumentLanguage.AR, "Attestation de présence (arabe)"),
            new Doc("ATTESTATION_PRESENCE", DocumentLanguage.FR, "Attestation de présence (français)"),
            new Doc("ATTESTATION_REUSSITE", DocumentLanguage.FR, "Attestation de réussite"),
            new Doc("RELEVE_NOTES", DocumentLanguage.FR, "Relevé des notes"));

    private static final List<String[]> KINSHIPS = List.of(
            new String[] {"PERE", "Père"}, new String[] {"MERE", "Mère"}, new String[] {"TUTEUR", "Tuteur"},
            new String[] {"FRERE", "Frère"}, new String[] {"SOEUR", "Sœur"},
            new String[] {"GRAND_PARENT", "Grand-parent"}, new String[] {"AUTRE", "Autre"});

    private final EstablishmentDirectory establishments;
    private final KinshipRepository kinships;
    private final DocumentTypeRepository documentTypes;
    private final StudentRepository students;
    private final StudentService studentService;
    private final EnrollmentService enrollmentService;
    private final ParametrageDirectory parametrage;
    private final boolean demoData;
    private final TransactionTemplate tx;

    ScolariteBootstrap(EstablishmentDirectory establishments, KinshipRepository kinships,
            DocumentTypeRepository documentTypes, StudentRepository students, StudentService studentService,
            EnrollmentService enrollmentService, ParametrageDirectory parametrage, EducatedProperties properties,
            PlatformTransactionManager transactionManager) {
        this.establishments = establishments;
        this.kinships = kinships;
        this.documentTypes = documentTypes;
        this.students = students;
        this.studentService = studentService;
        this.enrollmentService = enrollmentService;
        this.parametrage = parametrage;
        this.demoData = properties.bootstrap().demoData();
        this.tx = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        for (var establishmentId : establishments.allEstablishmentIds()) {
            TenantContext.runAs(establishmentId, () -> tx.executeWithoutResult(status -> {
                createReferenceData();
                if (demoData) {
                    createDemoStudent();
                }
            }));
        }
    }

    private void createReferenceData() {
        for (String[] kinship : KINSHIPS) {
            if (kinships.findByCode(kinship[0]).isEmpty()) {
                Kinship k = new Kinship();
                k.setCode(kinship[0]);
                k.setLabel(LocalizedText.of(kinship[1]));
                kinships.save(k);
            }
        }
        for (Doc doc : DOCUMENTS) {
            if (!documentTypes.existsByCodeAndLanguage(doc.code(), doc.language())) {
                DocumentType type = new DocumentType();
                type.setCode(doc.code());
                type.setLanguage(doc.language());
                type.setLabel(LocalizedText.of(doc.label()));
                type.setActive(true);
                documentTypes.save(type);
            }
        }
    }

    private void createDemoStudent() {
        if (students.findByMatricule(DEMO_MATRICULE).isPresent()) {
            return;
        }
        var person = new PersonInfo("Demo", "Student", null, null, Gender.MALE, LocalDate.of(2003, 5, 14), "Tunis",
                "Tunisienne", null, null, "student@educated.local", null, null, null, null);
        var student = studentService.create(new StudentData(person, DEMO_MATRICULE, LocalDate.of(2022, 9, 15),
                "Mathématiques", null, 2022, null));
        parametrage.currentAcademicYear()
                .flatMap(year -> parametrage.schoolClassesOf(year.id()).stream().findFirst())
                .ifPresent(schoolClass -> enrollmentService.enroll(student.getId(), schoolClass.id(),
                        EnrollmentType.RE_ENROLLMENT, LocalDate.of(2026, 9, 1), false));
    }
}
