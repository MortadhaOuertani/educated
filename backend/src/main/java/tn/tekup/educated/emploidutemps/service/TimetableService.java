package tn.tekup.educated.emploidutemps.service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.IsoFields;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.emploidutemps.api.SessionCancelled;
import tn.tekup.educated.emploidutemps.api.SessionScheduled;
import tn.tekup.educated.emploidutemps.domain.GenerationMode;
import tn.tekup.educated.emploidutemps.domain.OccurrenceStatus;
import tn.tekup.educated.emploidutemps.domain.SessionOccurrence;
import tn.tekup.educated.emploidutemps.domain.SessionOccurrenceRepository;
import tn.tekup.educated.emploidutemps.domain.TimeSlotTemplate;
import tn.tekup.educated.emploidutemps.domain.TimeSlotTemplateRepository;
import tn.tekup.educated.emploidutemps.domain.Timetable;
import tn.tekup.educated.emploidutemps.domain.TimetableRepository;
import tn.tekup.educated.emploidutemps.domain.TimetableSlot;
import tn.tekup.educated.emploidutemps.domain.TimetableSlotRepository;
import tn.tekup.educated.emploidutemps.domain.TimetableStatus;
import tn.tekup.educated.emploidutemps.domain.WeekPattern;
import tn.tekup.educated.parametrage.api.AcademicPeriodInfo;
import tn.tekup.educated.parametrage.api.ParametrageDirectory;
import tn.tekup.educated.parametrage.api.SchoolClassInfo;
import tn.tekup.educated.pedagogie.api.MakeUpConfirmed;
import tn.tekup.educated.pedagogie.api.TeachingAssignmentInfo;
import tn.tekup.educated.pedagogie.api.TeachingDirectory;
import tn.tekup.educated.shared.domain.SessionType;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;

/**
 * Weekly timetables of the classes. Publishing a timetable creates the dated sessions of the period
 * (Programme des séances), which attendance calls and teacher hours are based on.
 */
@Service
@Transactional
public class TimetableService {

    public record SlotData(UUID teachingAssignmentId, UUID roomId, DayOfWeek dayOfWeek, LocalTime startTime,
            LocalTime endTime, WeekPattern weekPattern, String timeSlotCode) {
    }

    private final TimetableRepository timetables;
    private final TimetableSlotRepository slots;
    private final SessionOccurrenceRepository sessions;
    private final TimeSlotTemplateRepository timeSlots;
    private final ParametrageDirectory parametrage;
    private final TeachingDirectory teaching;
    private final ApplicationEventPublisher events;

    public TimetableService(TimetableRepository timetables, TimetableSlotRepository slots,
            SessionOccurrenceRepository sessions, TimeSlotTemplateRepository timeSlots,
            ParametrageDirectory parametrage, TeachingDirectory teaching, ApplicationEventPublisher events) {
        this.timetables = timetables;
        this.slots = slots;
        this.sessions = sessions;
        this.timeSlots = timeSlots;
        this.parametrage = parametrage;
        this.teaching = teaching;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<TimeSlotTemplate> timeSlots() {
        return timeSlots.findAllByOrderByOrderIndex();
    }

    @Transactional(readOnly = true)
    public Timetable timetable(UUID id) {
        return timetables.findById(id).orElseThrow(() -> new NotFoundException("Timetable", id));
    }

    @Transactional(readOnly = true)
    public List<Timetable> timetablesOfClass(UUID schoolClassId) {
        return timetables.findAllBySchoolClassIdOrderByCreatedAtDesc(schoolClassId);
    }

    public Timetable create(UUID schoolClassId, UUID academicPeriodId, String name) {
        SchoolClassInfo schoolClass = parametrage.findSchoolClass(schoolClassId)
                .orElseThrow(() -> new NotFoundException("SchoolClass", schoolClassId));
        AcademicPeriodInfo period = parametrage.findAcademicPeriod(academicPeriodId)
                .orElseThrow(() -> new NotFoundException("AcademicPeriod", academicPeriodId));
        if (!period.academicYearId().equals(schoolClass.academicYearId())) {
            throw new BusinessRuleException("timetable.period", "The period is not in the class's academic year.");
        }
        Timetable timetable = new Timetable();
        timetable.setSchoolClassId(schoolClassId);
        timetable.setAcademicPeriodId(academicPeriodId);
        timetable.setName(name != null ? name : schoolClass.code() + " - " + period.code());
        timetable.setValidFrom(period.startDate());
        timetable.setValidTo(period.endDate());
        timetable.setStatus(TimetableStatus.DRAFT);
        timetable.setGenerationMode(GenerationMode.MANUAL);
        timetable.setVersionNumber((int) timetables.countBySchoolClassIdAndAcademicPeriodId(schoolClassId,
                academicPeriodId) + 1);
        return timetables.save(timetable);
    }

    /** Adds a weekly slot. Rejected when the class, the teacher or the room is already busy at that time. */
    public TimetableSlot addSlot(UUID timetableId, SlotData data) {
        Timetable timetable = draft(timetableId);
        TeachingAssignmentInfo assignment = teaching.findAssignment(data.teachingAssignmentId())
                .orElseThrow(() -> new NotFoundException("TeachingAssignment", data.teachingAssignmentId()));
        if (!assignment.schoolClassId().equals(timetable.getSchoolClassId())) {
            throw new BusinessRuleException("slot.assignment", "This subject is not taught to the timetable's class.");
        }
        TimeSlotTemplate template = data.timeSlotCode() == null ? null : timeSlots.findByCode(data.timeSlotCode())
                .orElseThrow(() -> new NotFoundException("TimeSlotTemplate", data.timeSlotCode()));
        LocalTime start = template != null ? template.getStartTime() : data.startTime();
        LocalTime end = template != null ? template.getEndTime() : data.endTime();
        if (start == null || end == null || !end.isAfter(start)) {
            throw new BusinessRuleException("slot.time", "The slot must end after it starts.");
        }
        WeekPattern pattern = data.weekPattern() == null ? WeekPattern.EVERY_WEEK : data.weekPattern();
        if (pattern == WeekPattern.CUSTOM) {
            throw new BusinessRuleException("slot.weekPattern", "Use every week, odd weeks or even weeks.");
        }
        if (data.roomId() != null && parametrage.findRoom(data.roomId()).isEmpty()) {
            throw new NotFoundException("Room", data.roomId());
        }

        for (TimetableSlot other : slots.findOverlapping(timetable.getAcademicPeriodId(), data.dayOfWeek(), start, end)) {
            if (!weeksOverlap(pattern, other.getWeekPattern())) {
                continue;
            }
            boolean sameTimetable = other.getTimetable().getId().equals(timetable.getId());
            boolean otherClass = !other.getTimetable().getSchoolClassId().equals(timetable.getSchoolClassId());
            if (sameTimetable) {
                throw conflict("The class already has a session at this time.");
            }
            if (otherClass && assignment.teacherId() != null && assignment.teacherId().equals(other.getTeacherId())) {
                throw conflict("The teacher already teaches another class at this time.");
            }
            if (otherClass && data.roomId() != null && data.roomId().equals(other.getRoomId())) {
                throw conflict("The room is already used at this time.");
            }
        }

        TimetableSlot slot = new TimetableSlot();
        slot.setTimetable(timetable);
        slot.setTeachingAssignmentId(assignment.id());
        slot.setTeacherId(assignment.teacherId());
        slot.setSubjectId(assignment.subjectId());
        slot.setRoomId(data.roomId());
        slot.setDayOfWeek(data.dayOfWeek());
        slot.setStartTime(start);
        slot.setEndTime(end);
        slot.setWeekPattern(pattern);
        slot.setTimeSlotTemplate(template);
        timetable.getSlots().add(slot);
        return slots.save(slot);
    }

    public void removeSlot(UUID timetableId, UUID slotId) {
        Timetable timetable = draft(timetableId);
        boolean removed = timetable.getSlots().removeIf(slot -> slot.getId().equals(slotId));
        if (!removed) {
            throw new NotFoundException("TimetableSlot", slotId);
        }
    }

    /**
     * Publishes the timetable and creates its sessions. A previously published version of the same class and period
     * is archived and its sessions from today on are cancelled.
     */
    public Timetable publish(UUID timetableId) {
        Timetable timetable = draft(timetableId);
        if (timetable.getSlots().isEmpty()) {
            throw new BusinessRuleException("timetable.empty", "Add at least one slot before publishing.");
        }
        LocalDate today = LocalDate.now();
        LocalDate from = timetable.getValidFrom();
        for (Timetable previous : timetables.findAllBySchoolClassIdAndAcademicPeriodIdAndStatus(
                timetable.getSchoolClassId(), timetable.getAcademicPeriodId(), TimetableStatus.PUBLISHED)) {
            previous.setStatus(TimetableStatus.ARCHIVED);
            for (SessionOccurrence session : sessions.findAllBySlot_Timetable_IdAndStatusAndSessionDateGreaterThanEqual(
                    previous.getId(), OccurrenceStatus.SCHEDULED, today)) {
                session.setStatus(OccurrenceStatus.CANCELLED);
                session.setCancellationReason("Remplacé par une nouvelle version de l'emploi du temps");
            }
            from = today.isAfter(from) ? today : from;
        }
        Map<UUID, SessionType> sessionTypes = new HashMap<>();
        for (TimetableSlot slot : timetable.getSlots()) {
            sessionTypes.put(slot.getId(), teaching.findAssignment(slot.getTeachingAssignmentId())
                    .map(TeachingAssignmentInfo::sessionType).orElse(SessionType.LECTURE));
        }
        for (LocalDate date = from; !date.isAfter(timetable.getValidTo()); date = date.plusDays(1)) {
            for (TimetableSlot slot : timetable.getSlots()) {
                if (slot.getDayOfWeek() == date.getDayOfWeek() && takesPlace(slot.getWeekPattern(), date)) {
                    sessions.save(occurrence(slot, date, sessionTypes.get(slot.getId())));
                }
            }
        }
        timetable.setStatus(TimetableStatus.PUBLISHED);
        timetable.setGeneratedAt(Instant.now());
        return timetable;
    }

    public SessionOccurrence cancelSession(UUID sessionId, String reason) {
        SessionOccurrence session = sessions.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("SessionOccurrence", sessionId));
        if (session.getStatus() != OccurrenceStatus.SCHEDULED) {
            throw new BusinessRuleException("session.notScheduled", "Only a scheduled session can be cancelled.");
        }
        session.setStatus(OccurrenceStatus.CANCELLED);
        session.setCancellationReason(reason);
        events.publishEvent(new SessionCancelled(session.getEstablishmentId(), session.getId(),
                session.getSchoolClassId(), session.getTeachingAssignmentId(), session.getSessionDate(), reason));
        return session;
    }

    @Transactional(readOnly = true)
    public List<SessionOccurrence> sessionsOfClass(UUID schoolClassId, LocalDate from, LocalDate to) {
        return sessions.findAllBySchoolClassIdAndSessionDateBetweenOrderBySessionDateAscStartTimeAsc(schoolClassId,
                from, to);
    }

    @Transactional(readOnly = true)
    public List<SessionOccurrence> sessionsOfTeacher(UUID teacherId, LocalDate from, LocalDate to) {
        return sessions.findAllByTeacherIdAndSessionDateBetweenOrderBySessionDateAscStartTimeAsc(teacherId, from, to);
    }

    /** Called after a make-up request is confirmed in the pedagogie module (own transaction, after its commit). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scheduleMakeUp(MakeUpConfirmed event) {
        TeachingAssignmentInfo assignment = teaching.findAssignment(event.teachingAssignmentId())
                .orElseThrow(() -> new NotFoundException("TeachingAssignment", event.teachingAssignmentId()));
        SessionOccurrence session = new SessionOccurrence();
        session.setTeachingAssignmentId(assignment.id());
        session.setTeacherId(assignment.teacherId());
        session.setSubjectId(assignment.subjectId());
        session.setSchoolClassId(assignment.schoolClassId());
        session.setRoomId(event.roomId());
        session.setSessionDate(event.date());
        session.setStartTime(event.startTime());
        session.setEndTime(event.endTime());
        session.setSessionType(assignment.sessionType());
        session.setStatus(OccurrenceStatus.SCHEDULED);
        session.setMakeUp(true);
        session.setMakeUpRequestId(event.makeUpRequestId());
        if (event.originalOccurrenceId() != null) {
            sessions.findById(event.originalOccurrenceId()).ifPresent(session::setMakeUpOf);
        }
        session = sessions.save(session);
        events.publishEvent(new SessionScheduled(session.getEstablishmentId(), session.getId(),
                session.getSchoolClassId(), session.getTeachingAssignmentId(), session.getSessionDate(),
                session.getStartTime(), true));
    }

    private Timetable draft(UUID timetableId) {
        Timetable timetable = timetable(timetableId);
        if (timetable.getStatus() != TimetableStatus.DRAFT) {
            throw new BusinessRuleException("timetable.notDraft", "Only a draft timetable can be changed.");
        }
        return timetable;
    }

    private static SessionOccurrence occurrence(TimetableSlot slot, LocalDate date, SessionType sessionType) {
        SessionOccurrence session = new SessionOccurrence();
        session.setSlot(slot);
        session.setTeachingAssignmentId(slot.getTeachingAssignmentId());
        session.setTeacherId(slot.getTeacherId());
        session.setSubjectId(slot.getSubjectId());
        session.setSchoolClassId(slot.getTimetable().getSchoolClassId());
        session.setRoomId(slot.getRoomId());
        session.setSessionDate(date);
        session.setStartTime(slot.getStartTime());
        session.setEndTime(slot.getEndTime());
        session.setSessionType(sessionType);
        session.setStatus(OccurrenceStatus.SCHEDULED);
        return session;
    }

    /** Odd-week and even-week slots never meet; every other combination does. */
    private static boolean weeksOverlap(WeekPattern a, WeekPattern b) {
        boolean oddAndEven = (a == WeekPattern.ODD_WEEKS && b == WeekPattern.EVEN_WEEKS)
                || (a == WeekPattern.EVEN_WEEKS && b == WeekPattern.ODD_WEEKS);
        return !oddAndEven;
    }

    private static boolean takesPlace(WeekPattern pattern, LocalDate date) {
        int week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        return switch (pattern) {
            case ODD_WEEKS -> week % 2 == 1;
            case EVEN_WEEKS -> week % 2 == 0;
            default -> true;
        };
    }

    private static BusinessRuleException conflict(String message) {
        return new BusinessRuleException("slot.conflict", message);
    }
}
