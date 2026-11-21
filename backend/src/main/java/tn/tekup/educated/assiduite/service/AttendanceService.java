package tn.tekup.educated.assiduite.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.assiduite.api.AbsencesRecorded;
import tn.tekup.educated.assiduite.domain.AttendancePolicy;
import tn.tekup.educated.assiduite.domain.AttendancePolicyRepository;
import tn.tekup.educated.assiduite.domain.AttendanceRecord;
import tn.tekup.educated.assiduite.domain.AttendanceRecordRepository;
import tn.tekup.educated.assiduite.domain.AttendanceSheet;
import tn.tekup.educated.assiduite.domain.AttendanceSheetRepository;
import tn.tekup.educated.assiduite.domain.AttendanceSheetStatus;
import tn.tekup.educated.assiduite.domain.AttendanceStatus;
import tn.tekup.educated.assiduite.domain.TeacherAttendance;
import tn.tekup.educated.assiduite.domain.TeacherAttendanceRepository;
import tn.tekup.educated.assiduite.domain.TeacherPresenceStatus;
import tn.tekup.educated.emploidutemps.api.ScheduleDirectory;
import tn.tekup.educated.emploidutemps.api.SessionInfo;
import tn.tekup.educated.scolarite.api.StudentDirectory;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;

/**
 * Appels (attendance calls). The teacher marks each student present, absent or late, then confirms; changes are
 * still allowed during the policy's edit window (48 h by default). Confirming records the teacher's hours.
 */
@Service
@Transactional
public class AttendanceService {

    public record RecordData(UUID studentId, AttendanceStatus status, Integer lateMinutes, String comment) {
    }

    /** One past session of the student that was not "present". */
    public record AbsenceLine(UUID recordId, SessionInfo session, AttendanceStatus status, Integer lateMinutes,
            boolean justified) {
    }

    /** Taux d'absence of a student in one subject: OK, AT_RISK (10 % by default) or CRITICAL (25 %). */
    public record SubjectStatistic(UUID teachingAssignmentId, int sessions, int unjustifiedAbsences,
            BigDecimal absencePercent, String level) {
    }

    private final AttendanceSheetRepository sheets;
    private final AttendanceRecordRepository records;
    private final TeacherAttendanceRepository teacherAttendance;
    private final AttendancePolicyRepository policies;
    private final ScheduleDirectory schedule;
    private final StudentDirectory students;
    private final ApplicationEventPublisher events;

    public AttendanceService(AttendanceSheetRepository sheets, AttendanceRecordRepository records,
            TeacherAttendanceRepository teacherAttendance, AttendancePolicyRepository policies,
            ScheduleDirectory schedule, StudentDirectory students, ApplicationEventPublisher events) {
        this.sheets = sheets;
        this.records = records;
        this.teacherAttendance = teacherAttendance;
        this.policies = policies;
        this.schedule = schedule;
        this.students = students;
        this.events = events;
    }

    /**
     * The call sheet of a session, created with every student of the class marked present when it is first opened.
     * When {@code teacherId} is given, the session must be one of that teacher's.
     */
    public AttendanceSheet openSheet(UUID sessionId, UUID teacherId) {
        SessionInfo session = session(sessionId, teacherId);
        return sheets.findBySessionOccurrenceId(sessionId).orElseGet(() -> {
            if ("CANCELLED".equals(session.status())) {
                throw new BusinessRuleException("attendance.cancelled", "This session was cancelled.");
            }
            if (session.date().isAfter(LocalDate.now())) {
                throw new BusinessRuleException("attendance.future", "The call can only be taken once the session has started.");
            }
            AttendanceSheet sheet = new AttendanceSheet();
            sheet.setSessionOccurrenceId(sessionId);
            sheet.setSchoolClassId(session.schoolClassId());
            sheet.setTeacherId(session.teacherId());
            sheet.setStatus(AttendanceSheetStatus.DRAFT);
            for (UUID studentId : students.studentIdsInClass(session.schoolClassId())) {
                AttendanceRecord record = new AttendanceRecord();
                record.setSheet(sheet);
                record.setStudentId(studentId);
                record.setStatus(AttendanceStatus.PRESENT);
                sheet.getRecords().add(record);
            }
            return sheets.save(sheet);
        });
    }

    public AttendanceSheet saveRecords(UUID sessionId, UUID teacherId, List<RecordData> changes) {
        AttendanceSheet sheet = openSheet(sessionId, teacherId);
        if (!isEditable(sheet)) {
            throw new BusinessRuleException("attendance.locked", "The call can no longer be changed.");
        }
        Map<UUID, AttendanceRecord> byStudent = new HashMap<>();
        sheet.getRecords().forEach(record -> byStudent.put(record.getStudentId(), record));
        for (RecordData change : changes) {
            AttendanceRecord record = byStudent.get(change.studentId());
            if (record == null) {
                throw new BusinessRuleException("attendance.student", "This student is not in the class.");
            }
            record.setStatus(change.status());
            record.setLateMinutes(change.status() == AttendanceStatus.LATE ? change.lateMinutes() : null);
            record.setComment(change.comment());
        }
        return sheet;
    }

    /** Confirms the call, opens the edit window, records the teacher's hours and tells who was absent or late. */
    public AttendanceSheet confirm(UUID sessionId, UUID teacherId) {
        AttendanceSheet sheet = openSheet(sessionId, teacherId);
        if (sheet.getStatus() != AttendanceSheetStatus.DRAFT) {
            throw new BusinessRuleException("attendance.alreadyConfirmed", "This call is already confirmed.");
        }
        SessionInfo session = session(sessionId, null);
        Instant now = Instant.now();
        sheet.setStatus(AttendanceSheetStatus.CONFIRMED);
        sheet.setConfirmedAt(now);
        sheet.setEditableUntil(now.plus(Duration.ofHours(policy().getEditWindowHours())));

        if (session.teacherId() != null && !teacherAttendance.existsBySessionOccurrenceId(sessionId)) {
            TeacherAttendance hours = new TeacherAttendance();
            hours.setSessionOccurrenceId(sessionId);
            hours.setTeacherId(session.teacherId());
            hours.setSessionDate(session.date());
            hours.setWeekNumber(session.date().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
            hours.setStatus(TeacherPresenceStatus.PRESENT);
            hours.setHours(session.hours());
            teacherAttendance.save(hours);
        }

        List<UUID> absent = new ArrayList<>();
        List<UUID> late = new ArrayList<>();
        for (AttendanceRecord record : sheet.getRecords()) {
            if (record.getStatus() == AttendanceStatus.ABSENT) {
                absent.add(record.getStudentId());
            } else if (record.getStatus() == AttendanceStatus.LATE) {
                late.add(record.getStudentId());
            }
        }
        events.publishEvent(new AbsencesRecorded(sheet.getEstablishmentId(), sessionId, session.schoolClassId(),
                session.teachingAssignmentId(), session.date(), absent, late));
        return sheet;
    }

    @Transactional(readOnly = true)
    public boolean isEditable(AttendanceSheet sheet) {
        return switch (sheet.getStatus()) {
            case DRAFT -> true;
            case CONFIRMED -> sheet.getEditableUntil() != null && Instant.now().isBefore(sheet.getEditableUntil());
            case LOCKED -> false;
        };
    }

    /** Absences and late arrivals of the student in confirmed calls, newest first. */
    @Transactional(readOnly = true)
    public List<AbsenceLine> absencesOf(UUID studentId) {
        List<AttendanceRecord> confirmed = confirmedRecords(studentId);
        Map<UUID, SessionInfo> sessions = sessionsOf(confirmed);
        return confirmed.stream()
                .filter(record -> record.getStatus() != AttendanceStatus.PRESENT)
                .map(record -> new AbsenceLine(record.getId(), sessions.get(record.getSheet().getSessionOccurrenceId()),
                        record.getStatus(), record.getLateMinutes(), record.isJustified()))
                .filter(line -> line.session() != null)
                .sorted(Comparator.comparing((AbsenceLine line) -> line.session().date()).reversed())
                .toList();
    }

    /** Absence rate per subject, compared to the thresholds of the attendance policy. */
    @Transactional(readOnly = true)
    public List<SubjectStatistic> statisticsOf(UUID studentId) {
        List<AttendanceRecord> confirmed = confirmedRecords(studentId);
        Map<UUID, SessionInfo> sessions = sessionsOf(confirmed);
        Map<UUID, int[]> counts = new LinkedHashMap<>();
        for (AttendanceRecord record : confirmed) {
            SessionInfo session = sessions.get(record.getSheet().getSessionOccurrenceId());
            if (session == null) {
                continue;
            }
            int[] count = counts.computeIfAbsent(session.teachingAssignmentId(), key -> new int[2]);
            count[0]++;
            if (record.getStatus() == AttendanceStatus.ABSENT && !record.isJustified()) {
                count[1]++;
            }
        }
        AttendancePolicy policy = policy();
        List<SubjectStatistic> result = new ArrayList<>();
        counts.forEach((assignmentId, count) -> {
            BigDecimal percent = BigDecimal.valueOf(count[1] * 100L)
                    .divide(BigDecimal.valueOf(count[0]), 2, RoundingMode.HALF_UP);
            String level = percent.compareTo(policy.getCriticalPercent()) >= 0 ? "CRITICAL"
                    : percent.compareTo(policy.getAtRiskPercent()) >= 0 ? "AT_RISK" : "OK";
            result.add(new SubjectStatistic(assignmentId, count[0], count[1], percent, level));
        });
        return result;
    }

    @Transactional(readOnly = true)
    public AttendancePolicy policy() {
        return policies.findFirstByStudyLevelIdIsNull()
                .orElseThrow(() -> new NotFoundException("AttendancePolicy", "default"));
    }

    private List<AttendanceRecord> confirmedRecords(UUID studentId) {
        List<AttendanceRecord> result = new ArrayList<>(
                records.findAllByStudentIdAndSheet_Status(studentId, AttendanceSheetStatus.CONFIRMED));
        result.addAll(records.findAllByStudentIdAndSheet_Status(studentId, AttendanceSheetStatus.LOCKED));
        return result;
    }

    private Map<UUID, SessionInfo> sessionsOf(List<AttendanceRecord> list) {
        List<UUID> ids = list.stream().map(record -> record.getSheet().getSessionOccurrenceId()).distinct().toList();
        Map<UUID, SessionInfo> result = new HashMap<>();
        schedule.findSessions(ids).forEach(session -> result.put(session.id(), session));
        return result;
    }

    private SessionInfo session(UUID sessionId, UUID teacherId) {
        SessionInfo session = schedule.findSession(sessionId)
                .orElseThrow(() -> new NotFoundException("SessionOccurrence", sessionId));
        if (teacherId != null && !teacherId.equals(session.teacherId())) {
            throw new NotFoundException("SessionOccurrence", sessionId);
        }
        return session;
    }
}
