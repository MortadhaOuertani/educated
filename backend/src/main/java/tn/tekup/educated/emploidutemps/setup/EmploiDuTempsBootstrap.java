package tn.tekup.educated.emploidutemps.setup;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tn.tekup.educated.emploidutemps.domain.TimeSlotTemplate;
import tn.tekup.educated.emploidutemps.domain.TimeSlotTemplateRepository;
import tn.tekup.educated.emploidutemps.domain.Timetable;
import tn.tekup.educated.emploidutemps.domain.TimetableRepository;
import tn.tekup.educated.emploidutemps.domain.WeekPattern;
import tn.tekup.educated.emploidutemps.service.TimetableService;
import tn.tekup.educated.emploidutemps.service.TimetableService.SlotData;
import tn.tekup.educated.parametrage.api.ParametrageDirectory;
import tn.tekup.educated.parametrage.api.RoomInfo;
import tn.tekup.educated.parametrage.api.SchoolClassInfo;
import tn.tekup.educated.pedagogie.api.TeachingAssignmentInfo;
import tn.tekup.educated.pedagogie.api.TeachingDirectory;
import tn.tekup.educated.shared.EducatedProperties;
import tn.tekup.educated.shared.security.EstablishmentDirectory;
import tn.tekup.educated.shared.tenant.TenantContext;

/** The standard time slots and, in development, a published first-semester timetable for the demo class. */
@Component
@Order(45)
class EmploiDuTempsBootstrap implements ApplicationRunner {

    private static final List<String[]> TIME_SLOTS = List.of(
            new String[] {"S1", "08:30", "10:00"}, new String[] {"S2", "10:15", "11:45"},
            new String[] {"S3", "12:00", "13:30"}, new String[] {"S4", "14:00", "15:30"},
            new String[] {"S5", "15:45", "17:15"});

    private final EstablishmentDirectory establishments;
    private final TimeSlotTemplateRepository timeSlots;
    private final TimetableRepository timetables;
    private final TimetableService timetableService;
    private final ParametrageDirectory parametrage;
    private final TeachingDirectory teaching;
    private final boolean demoData;
    private final TransactionTemplate tx;

    EmploiDuTempsBootstrap(EstablishmentDirectory establishments, TimeSlotTemplateRepository timeSlots,
            TimetableRepository timetables, TimetableService timetableService, ParametrageDirectory parametrage,
            TeachingDirectory teaching, EducatedProperties properties, PlatformTransactionManager transactionManager) {
        this.establishments = establishments;
        this.timeSlots = timeSlots;
        this.timetables = timetables;
        this.timetableService = timetableService;
        this.parametrage = parametrage;
        this.teaching = teaching;
        this.demoData = properties.bootstrap().demoData();
        this.tx = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        for (var establishmentId : establishments.allEstablishmentIds()) {
            TenantContext.runAs(establishmentId, () -> tx.executeWithoutResult(status -> {
                for (int i = 0; i < TIME_SLOTS.size(); i++) {
                    String[] slot = TIME_SLOTS.get(i);
                    if (timeSlots.findByCode(slot[0]).isEmpty()) {
                        TimeSlotTemplate template = new TimeSlotTemplate();
                        template.setCode(slot[0]);
                        template.setStartTime(LocalTime.parse(slot[1]));
                        template.setEndTime(LocalTime.parse(slot[2]));
                        template.setOrderIndex(i + 1);
                        timeSlots.save(template);
                    }
                }
                if (demoData) {
                    parametrage.currentAcademicYear()
                            .flatMap(year -> parametrage.schoolClassesOf(year.id()).stream().findFirst())
                            .filter(schoolClass -> timetables.findAllBySchoolClassIdOrderByCreatedAtDesc(
                                    schoolClass.id()).isEmpty())
                            .ifPresent(this::createDemoTimetable);
                }
            }));
        }
    }

    /** Monday morning: the lecture; Wednesday midday: the lab. Both in the demo room. */
    private void createDemoTimetable(SchoolClassInfo schoolClass) {
        List<TeachingAssignmentInfo> assignments = teaching.assignmentsOfClass(schoolClass.id());
        if (assignments.size() < 2) {
            return;
        }
        var firstPeriod = parametrage.periodsOf(schoolClass.academicYearId()).stream().findFirst();
        if (firstPeriod.isEmpty()) {
            return;
        }
        var room = parametrage.rooms().stream().findFirst().map(RoomInfo::id).orElse(null);
        Timetable timetable = timetableService.create(schoolClass.id(), firstPeriod.get().id(), null);
        timetableService.addSlot(timetable.getId(), new SlotData(assignments.get(0).id(), room, DayOfWeek.MONDAY,
                null, null, WeekPattern.EVERY_WEEK, "S1"));
        timetableService.addSlot(timetable.getId(), new SlotData(assignments.get(1).id(), room, DayOfWeek.WEDNESDAY,
                null, null, WeekPattern.EVERY_WEEK, "S3"));
        timetableService.publish(timetable.getId());
    }
}
