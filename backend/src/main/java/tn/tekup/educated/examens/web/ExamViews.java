package tn.tekup.educated.examens.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tn.tekup.educated.examens.domain.Exam;
import tn.tekup.educated.examens.domain.ExamSession;
import tn.tekup.educated.examens.domain.GradeEntry;
import tn.tekup.educated.examens.domain.GradeGrid;
import tn.tekup.educated.examens.domain.Invigilation;
import tn.tekup.educated.examens.domain.ReportCard;
import tn.tekup.educated.examens.service.ReportCardService;
import tn.tekup.educated.parametrage.api.ParametrageDirectory;
import tn.tekup.educated.parametrage.api.RoomInfo;
import tn.tekup.educated.parametrage.api.SchoolClassInfo;
import tn.tekup.educated.pedagogie.api.StudyPlanSubjectInfo;
import tn.tekup.educated.pedagogie.api.TeacherInfo;
import tn.tekup.educated.pedagogie.api.TeachingDirectory;
import tn.tekup.educated.scolarite.api.StudentDirectory;
import tn.tekup.educated.scolarite.api.StudentInfo;

/** Builds the JSON views of the exams module, with subject, class, room, teacher and student names. */
@Component
class ExamViews {

    record SessionDto(UUID id, String name, String sessionType, UUID academicPeriodId, LocalDate startDate,
            LocalDate endDate, boolean published) {
        static SessionDto from(ExamSession s) {
            return new SessionDto(s.getId(), s.getName(), s.getSessionType().name(), s.getAcademicPeriodId(),
                    s.getStartDate(), s.getEndDate(), s.isPublished());
        }
    }

    record ExamDto(UUID id, String subjectCode, String subjectName, String assessmentType, LocalDate examDate,
            LocalTime startTime, LocalTime endTime, Integer durationMinutes, List<String> classCodes,
            List<String> roomCodes, List<String> invigilators, boolean published) {
    }

    record InvigilationDto(UUID examId, String subjectName, LocalDate examDate, LocalTime startTime,
            LocalTime endTime, String roomCode, String role) {
    }

    record GradeRowDto(UUID studentId, String matricule, String fullName, BigDecimal score, String reason,
            String comment) {
    }

    record GridDto(UUID id, String subjectCode, String subjectName, UUID schoolClassId, String classCode,
            UUID academicPeriodId, String assessmentType, BigDecimal maxScore, String status, Instant confirmedAt,
            Instant publishedAt, List<GradeRowDto> rows) {
    }

    record StudentGradeDto(String subjectCode, String subjectName, String assessmentType, BigDecimal score,
            BigDecimal maxScore, String reason, Instant publishedAt) {
    }

    record SubjectResultDto(String subjectCode, String subjectName, BigDecimal coefficient, BigDecimal average,
            Integer creditsEarned, boolean validated) {
    }

    record ReportCardDto(UUID id, UUID studentId, String matricule, String fullName, UUID academicPeriodId,
            BigDecimal average, Integer rankInClass, Integer creditsEarned, String appreciation, boolean published,
            List<SubjectResultDto> subjects) {
    }

    private final TeachingDirectory teaching;
    private final ParametrageDirectory parametrage;
    private final StudentDirectory students;
    private final ReportCardService reportCards;

    ExamViews(TeachingDirectory teaching, ParametrageDirectory parametrage, StudentDirectory students,
            ReportCardService reportCards) {
        this.teaching = teaching;
        this.parametrage = parametrage;
        this.students = students;
        this.reportCards = reportCards;
    }

    ExamDto exam(Exam e) {
        Optional<StudyPlanSubjectInfo> subject = teaching.findStudyPlanSubject(e.getStudyPlanSubjectId());
        return new ExamDto(e.getId(), subject.map(StudyPlanSubjectInfo::subjectCode).orElse(null),
                subject.map(StudyPlanSubjectInfo::subjectName).orElse(null), e.getAssessmentType().name(),
                e.getExamDate(), e.getStartTime(), e.getEndTime(), e.getDurationMinutes(),
                e.getClasses().stream().map(c -> classCode(c.getSchoolClassId())).toList(),
                e.getRooms().stream().map(r -> roomCode(r.getRoomId())).toList(),
                e.getInvigilations().stream().map(i -> teaching.findTeacher(i.getTeacherId())
                        .map(TeacherInfo::fullName).orElse(null)).toList(),
                e.isPublished());
    }

    InvigilationDto invigilation(Invigilation i) {
        Exam e = i.getExam();
        return new InvigilationDto(e.getId(), teaching.findStudyPlanSubject(e.getStudyPlanSubjectId())
                .map(StudyPlanSubjectInfo::subjectName).orElse(null), e.getExamDate(), e.getStartTime(),
                e.getEndTime(), roomCode(i.getRoomId()), i.getRole().name());
    }

    /** One row per student of the class, graded or not yet. */
    GridDto grid(GradeGrid g) {
        Map<UUID, GradeEntry> entries = new HashMap<>();
        g.getEntries().forEach(entry -> entries.put(entry.getStudentId(), entry));
        List<GradeRowDto> rows = students.studentIdsInClass(g.getSchoolClassId()).stream().map(studentId -> {
            Optional<StudentInfo> student = students.findStudent(studentId);
            GradeEntry entry = entries.get(studentId);
            return new GradeRowDto(studentId, student.map(StudentInfo::matricule).orElse(null),
                    student.map(StudentInfo::fullName).orElse(null), entry == null ? null : entry.getScore(),
                    entry == null || entry.getReason() == null ? null : entry.getReason().name(),
                    entry == null ? null : entry.getComment());
        }).sorted(Comparator.comparing(GradeRowDto::fullName, Comparator.nullsLast(Comparator.naturalOrder()))).toList();
        Optional<StudyPlanSubjectInfo> subject = teaching.findStudyPlanSubject(g.getStudyPlanSubjectId());
        return new GridDto(g.getId(), subject.map(StudyPlanSubjectInfo::subjectCode).orElse(null),
                subject.map(StudyPlanSubjectInfo::subjectName).orElse(null), g.getSchoolClassId(),
                classCode(g.getSchoolClassId()), g.getAcademicPeriodId(), g.getAssessmentType().name(),
                g.getMaxScore(), g.getStatus().name(), g.getConfirmedAt(), g.getPublishedAt(), rows);
    }

    StudentGradeDto studentGrade(GradeEntry e) {
        GradeGrid g = e.getGrid();
        Optional<StudyPlanSubjectInfo> subject = teaching.findStudyPlanSubject(g.getStudyPlanSubjectId());
        return new StudentGradeDto(subject.map(StudyPlanSubjectInfo::subjectCode).orElse(null),
                subject.map(StudyPlanSubjectInfo::subjectName).orElse(null), g.getAssessmentType().name(),
                e.getScore(), g.getMaxScore(), e.getReason() == null ? null : e.getReason().name(), g.getPublishedAt());
    }

    ReportCardDto reportCard(ReportCard c) {
        Optional<StudentInfo> student = students.findStudent(c.getStudentId());
        List<SubjectResultDto> subjects = reportCards.resultsOf(c.getId()).stream().map(r -> {
            Optional<StudyPlanSubjectInfo> subject = teaching.findStudyPlanSubject(r.getStudyPlanSubjectId());
            return new SubjectResultDto(subject.map(StudyPlanSubjectInfo::subjectCode).orElse(null),
                    subject.map(StudyPlanSubjectInfo::subjectName).orElse(null),
                    subject.map(StudyPlanSubjectInfo::coefficient).orElse(null), r.getAverage(),
                    r.getCreditsEarned(), r.isValidated());
        }).toList();
        return new ReportCardDto(c.getId(), c.getStudentId(), student.map(StudentInfo::matricule).orElse(null),
                student.map(StudentInfo::fullName).orElse(null), c.getAcademicPeriodId(), c.getAverage(),
                c.getRankInClass(), c.getCreditsEarned(), c.getAppreciationText(), c.isPublished(), subjects);
    }

    private String classCode(UUID id) {
        return parametrage.findSchoolClass(id).map(SchoolClassInfo::code).orElse(null);
    }

    private String roomCode(UUID id) {
        return id == null ? null : parametrage.findRoom(id).map(RoomInfo::code).orElse(null);
    }
}
