package tn.tekup.educated.assiduite.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tn.tekup.educated.assiduite.domain.AttendanceSheet;
import tn.tekup.educated.assiduite.domain.AttendanceStatus;
import tn.tekup.educated.assiduite.domain.RequestStatus;
import tn.tekup.educated.assiduite.domain.StudentAbsenceRequest;
import tn.tekup.educated.assiduite.domain.TeacherAttendance;
import tn.tekup.educated.assiduite.service.AttendanceService;
import tn.tekup.educated.assiduite.service.AttendanceService.AbsenceLine;
import tn.tekup.educated.assiduite.service.AttendanceService.SubjectStatistic;
import tn.tekup.educated.emploidutemps.api.ScheduleDirectory;
import tn.tekup.educated.emploidutemps.api.SessionInfo;
import tn.tekup.educated.pedagogie.api.TeachingAssignmentInfo;
import tn.tekup.educated.pedagogie.api.TeachingDirectory;
import tn.tekup.educated.scolarite.api.StudentDirectory;
import tn.tekup.educated.scolarite.api.StudentInfo;

/** Builds the JSON views of the attendance module, with student and subject names. */
@Component
class AttendanceViews {

    record RecordDto(UUID studentId, String matricule, String fullName, AttendanceStatus status, Integer lateMinutes,
            boolean justified, String comment) {
    }

    record SheetDto(UUID sessionId, LocalDate date, LocalTime startTime, LocalTime endTime, String subjectName,
            UUID schoolClassId, String status, Instant confirmedAt, Instant editableUntil, boolean editable,
            List<RecordDto> records) {
    }

    record AbsenceLineDto(UUID recordId, UUID sessionId, LocalDate date, LocalTime startTime, LocalTime endTime,
            String subjectName, AttendanceStatus status, Integer lateMinutes, boolean justified) {
    }

    record StatisticDto(UUID teachingAssignmentId, String subjectName, int sessions, int unjustifiedAbsences,
            BigDecimal absencePercent, String level) {
    }

    record AbsencesDto(List<AbsenceLineDto> absences, List<StatisticDto> statistics) {
    }

    record AbsenceRequestDto(UUID id, UUID studentId, String matricule, String fullName, LocalDateTime fromDateTime,
            LocalDateTime toDateTime, String reason, boolean hasProof, RequestStatus status, Instant reviewedAt,
            Instant createdAt) {
    }

    record TeacherHoursDto(UUID id, UUID sessionId, LocalDate sessionDate, Integer weekNumber, String status,
            BigDecimal hours, boolean validated, Instant validatedAt) {
        static TeacherHoursDto from(TeacherAttendance t) {
            return new TeacherHoursDto(t.getId(), t.getSessionOccurrenceId(), t.getSessionDate(), t.getWeekNumber(),
                    t.getStatus().name(), t.getHours(), t.isValidated(), t.getValidatedAt());
        }
    }

    private final AttendanceService attendance;
    private final ScheduleDirectory schedule;
    private final StudentDirectory students;
    private final TeachingDirectory teaching;

    AttendanceViews(AttendanceService attendance, ScheduleDirectory schedule, StudentDirectory students,
            TeachingDirectory teaching) {
        this.attendance = attendance;
        this.schedule = schedule;
        this.students = students;
        this.teaching = teaching;
    }

    SheetDto sheet(AttendanceSheet sheet) {
        SessionInfo session = schedule.findSession(sheet.getSessionOccurrenceId()).orElseThrow();
        List<RecordDto> records = sheet.getRecords().stream().map(record -> {
            Optional<StudentInfo> student = students.findStudent(record.getStudentId());
            return new RecordDto(record.getStudentId(), student.map(StudentInfo::matricule).orElse(null),
                    student.map(StudentInfo::fullName).orElse(null), record.getStatus(), record.getLateMinutes(),
                    record.isJustified(), record.getComment());
        }).sorted(Comparator.comparing(RecordDto::fullName, Comparator.nullsLast(Comparator.naturalOrder()))).toList();
        return new SheetDto(session.id(), session.date(), session.startTime(), session.endTime(),
                subjectName(session.teachingAssignmentId(), new HashMap<>()), session.schoolClassId(),
                sheet.getStatus().name(), sheet.getConfirmedAt(), sheet.getEditableUntil(),
                attendance.isEditable(sheet), records);
    }

    AbsencesDto absences(UUID studentId) {
        Map<UUID, String> subjects = new HashMap<>();
        List<AbsenceLineDto> lines = attendance.absencesOf(studentId).stream().map(line -> line(line, subjects)).toList();
        List<StatisticDto> statistics = attendance.statisticsOf(studentId).stream()
                .map(s -> statistic(s, subjects))
                .toList();
        return new AbsencesDto(lines, statistics);
    }

    AbsenceRequestDto request(StudentAbsenceRequest r) {
        Optional<StudentInfo> student = students.findStudent(r.getStudentId());
        return new AbsenceRequestDto(r.getId(), r.getStudentId(), student.map(StudentInfo::matricule).orElse(null),
                student.map(StudentInfo::fullName).orElse(null), r.getFromDateTime(), r.getToDateTime(),
                r.getReason(), r.getProofFileId() != null, r.getStatus(), r.getReviewedAt(), r.getCreatedAt());
    }

    private AbsenceLineDto line(AbsenceLine line, Map<UUID, String> subjects) {
        SessionInfo s = line.session();
        return new AbsenceLineDto(line.recordId(), s.id(), s.date(), s.startTime(), s.endTime(),
                subjectName(s.teachingAssignmentId(), subjects), line.status(), line.lateMinutes(), line.justified());
    }

    private StatisticDto statistic(SubjectStatistic s, Map<UUID, String> subjects) {
        return new StatisticDto(s.teachingAssignmentId(), subjectName(s.teachingAssignmentId(), subjects), s.sessions(),
                s.unjustifiedAbsences(), s.absencePercent(), s.level());
    }

    private String subjectName(UUID assignmentId, Map<UUID, String> cache) {
        return cache.computeIfAbsent(assignmentId, id -> teaching.findAssignment(id)
                .map(TeachingAssignmentInfo::subjectName).orElse(""));
    }
}
