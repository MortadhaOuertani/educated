package tn.tekup.educated.pedagogie.setup;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tn.tekup.educated.parametrage.api.ParametrageDirectory;
import tn.tekup.educated.parametrage.api.SchoolClassInfo;
import tn.tekup.educated.pedagogie.domain.EmploymentType;
import tn.tekup.educated.pedagogie.domain.StudyPlan;
import tn.tekup.educated.pedagogie.domain.StudyPlanStatus;
import tn.tekup.educated.pedagogie.domain.StudyPlanSubject;
import tn.tekup.educated.pedagogie.domain.Subject;
import tn.tekup.educated.pedagogie.domain.Teacher;
import tn.tekup.educated.pedagogie.domain.TeacherGradeRepository;
import tn.tekup.educated.pedagogie.domain.TeacherRepository;
import tn.tekup.educated.pedagogie.service.CatalogService;
import tn.tekup.educated.pedagogie.service.CatalogService.AssessmentData;
import tn.tekup.educated.pedagogie.service.CatalogService.PlanSubjectData;
import tn.tekup.educated.pedagogie.service.CatalogService.StudyPlanData;
import tn.tekup.educated.pedagogie.service.CatalogService.SubjectData;
import tn.tekup.educated.pedagogie.service.CatalogService.UnitData;
import tn.tekup.educated.pedagogie.service.TeachingService;
import tn.tekup.educated.pedagogie.service.TeachingService.TeacherData;
import tn.tekup.educated.shared.EducatedProperties;
import tn.tekup.educated.shared.domain.AssessmentType;
import tn.tekup.educated.shared.domain.Gender;
import tn.tekup.educated.shared.domain.LocalizedText;
import tn.tekup.educated.shared.domain.Money;
import tn.tekup.educated.shared.domain.PersonInfo;
import tn.tekup.educated.shared.domain.SessionType;
import tn.tekup.educated.shared.security.EstablishmentDirectory;
import tn.tekup.educated.shared.tenant.TenantContext;

/** Teacher grades every establishment needs and, in development, a demo teacher who teaches the demo class. */
@Component
@Order(35)
class PedagogieBootstrap implements ApplicationRunner {

    static final String DEMO_EMPLOYEE_NUMBER = "DEMO-T-001";

    private static final List<String[]> GRADES = List.of(
            new String[] {"PROF", "Professeur"}, new String[] {"MC", "Maître de conférences"},
            new String[] {"MA", "Maître assistant"}, new String[] {"AS", "Assistant"},
            new String[] {"ING", "Ingénieur"}, new String[] {"EXP", "Expert"});

    private final EstablishmentDirectory establishments;
    private final TeacherGradeRepository grades;
    private final TeacherRepository teachers;
    private final CatalogService catalog;
    private final TeachingService teaching;
    private final ParametrageDirectory parametrage;
    private final boolean demoData;
    private final TransactionTemplate tx;

    PedagogieBootstrap(EstablishmentDirectory establishments, TeacherGradeRepository grades,
            TeacherRepository teachers, CatalogService catalog, TeachingService teaching,
            ParametrageDirectory parametrage, EducatedProperties properties,
            PlatformTransactionManager transactionManager) {
        this.establishments = establishments;
        this.grades = grades;
        this.teachers = teachers;
        this.catalog = catalog;
        this.teaching = teaching;
        this.parametrage = parametrage;
        this.demoData = properties.bootstrap().demoData();
        this.tx = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        for (var establishmentId : establishments.allEstablishmentIds()) {
            TenantContext.runAs(establishmentId, () -> tx.executeWithoutResult(status -> {
                for (String[] grade : GRADES) {
                    if (grades.findByCode(grade[0]).isEmpty()) {
                        catalog.createGrade(grade[0], LocalizedText.of(grade[1]));
                    }
                }
                if (demoData && teachers.findByEmployeeNumber(DEMO_EMPLOYEE_NUMBER).isEmpty()) {
                    parametrage.currentAcademicYear()
                            .flatMap(year -> parametrage.schoolClassesOf(year.id()).stream().findFirst())
                            .ifPresent(this::createDemoTeaching);
                }
            }));
        }
    }

    /** Two subjects of the demo class's study plan, both taught by the demo teacher. */
    private void createDemoTeaching(SchoolClassInfo schoolClass) {
        Subject architecture = catalog.createSubject(new SubjectData("GL-ARCHI", LocalizedText.of("Architecture logicielle"),
                null, null, new BigDecimal("2"), 4));
        Subject jee = catalog.createSubject(new SubjectData("GL-JEE", LocalizedText.of("Développement Jakarta EE"),
                null, null, new BigDecimal("1.5"), 3));
        List<AssessmentData> examAndTest = List.of(
                new AssessmentData(AssessmentType.EXAM, new BigDecimal("60"), null),
                new AssessmentData(AssessmentType.TEST, new BigDecimal("40"), null));
        StudyPlan plan = catalog.createStudyPlan(new StudyPlanData("DEMO-" + schoolClass.code() + "-S1", null,
                schoolClass.studyLevelId(), 1, List.of(new UnitData("UE-GL", LocalizedText.of("Génie logiciel"),
                        new BigDecimal("3.5"), 7, List.of(
                                new PlanSubjectData(architecture.getId(), null, null, 21, 0, 0, 0, examAndTest),
                                new PlanSubjectData(jee.getId(), null, null, 0, 0, 21, 0, examAndTest))))));
        catalog.changeStatus(plan.getId(), StudyPlanStatus.ACTIVE);

        var person = new PersonInfo("Demo", "Teacher", null, null, Gender.FEMALE, LocalDate.of(1985, 3, 2), null, null,
                null, null, "teacher@educated.local", null, null, null, null);
        Teacher teacher = teaching.createTeacher(new TeacherData(person, DEMO_EMPLOYEE_NUMBER, "MA",
                EmploymentType.PERMANENT, LocalDate.of(2015, 9, 1), Money.tnd(new BigDecimal("45")), 192, 18));
        List<StudyPlanSubject> planSubjects = plan.getUnits().getFirst().getSubjects();
        teaching.createAssignment(planSubjects.get(0).getId(), schoolClass.id(), SessionType.LECTURE, 21, teacher.getId());
        teaching.createAssignment(planSubjects.get(1).getId(), schoolClass.id(), SessionType.LAB, 21, teacher.getId());
    }
}
