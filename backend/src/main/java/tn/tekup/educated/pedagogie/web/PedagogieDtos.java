package tn.tekup.educated.pedagogie.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import tn.tekup.educated.pedagogie.domain.CourseJournalEntry;
import tn.tekup.educated.pedagogie.domain.CourseMaterial;
import tn.tekup.educated.pedagogie.domain.EmploymentType;
import tn.tekup.educated.pedagogie.domain.Homework;
import tn.tekup.educated.pedagogie.domain.MakeUpRequest;
import tn.tekup.educated.pedagogie.domain.MakeUpStatus;
import tn.tekup.educated.pedagogie.domain.StudyPlan;
import tn.tekup.educated.pedagogie.domain.StudyPlanStatus;
import tn.tekup.educated.pedagogie.domain.StudyPlanSubject;
import tn.tekup.educated.pedagogie.domain.Subject;
import tn.tekup.educated.pedagogie.domain.SubjectAssessment;
import tn.tekup.educated.pedagogie.domain.Teacher;
import tn.tekup.educated.pedagogie.domain.TeacherGrade;
import tn.tekup.educated.pedagogie.domain.TeachingAssignment;
import tn.tekup.educated.pedagogie.domain.TeachingUnit;
import tn.tekup.educated.shared.domain.AssessmentType;
import tn.tekup.educated.shared.domain.LocalizedText;
import tn.tekup.educated.shared.domain.Money;
import tn.tekup.educated.shared.domain.PersonInfo;
import tn.tekup.educated.shared.domain.SessionType;

/** Request and response bodies of the pedagogie REST API. */
final class PedagogieDtos {

    private PedagogieDtos() {
    }

    record TeacherGradeDto(UUID id, String code, LocalizedText label) {
        static TeacherGradeDto from(TeacherGrade g) {
            return new TeacherGradeDto(g.getId(), g.getCode(), g.getLabel());
        }
    }

    record TeacherGradeRequest(@NotBlank @Size(max = 20) String code, @NotNull LocalizedText label) {
    }

    record SubjectDto(UUID id, String code, LocalizedText name, String description, UUID teachingDomainId,
            BigDecimal defaultCoefficient, Integer defaultCredits, boolean active) {
        static SubjectDto from(Subject s) {
            return new SubjectDto(s.getId(), s.getCode(), s.getName(), s.getDescription(), s.getTeachingDomainId(),
                    s.getDefaultCoefficient(), s.getDefaultCredits(), s.isActive());
        }
    }

    record SubjectRequest(@NotBlank @Size(max = 40) String code, @NotNull LocalizedText name, String description,
            UUID teachingDomainId, BigDecimal defaultCoefficient, Integer defaultCredits) {
    }

    record AssessmentDto(AssessmentType assessmentType, BigDecimal weightPercent, BigDecimal maxScore) {
        static AssessmentDto from(SubjectAssessment a) {
            return new AssessmentDto(a.getAssessmentType(), a.getWeightPercent(), a.getMaxScore());
        }
    }

    record PlanSubjectDto(UUID id, UUID subjectId, String subjectCode, LocalizedText subjectName,
            BigDecimal coefficient, Integer credits, Integer lectureHours, Integer tutorialHours, Integer labHours,
            Integer projectHours, List<AssessmentDto> assessments) {
        static PlanSubjectDto from(StudyPlanSubject s) {
            return new PlanSubjectDto(s.getId(), s.getSubject().getId(), s.getSubject().getCode(),
                    s.getSubject().getName(), s.getCoefficient(), s.getCredits(), s.getLectureHours(),
                    s.getTutorialHours(), s.getLabHours(), s.getProjectHours(),
                    s.getAssessments().stream().map(AssessmentDto::from).toList());
        }
    }

    record UnitDto(UUID id, String code, LocalizedText name, BigDecimal coefficient, Integer credits,
            List<PlanSubjectDto> subjects) {
        static UnitDto from(TeachingUnit u) {
            return new UnitDto(u.getId(), u.getCode(), u.getName(), u.getCoefficient(), u.getCredits(),
                    u.getSubjects().stream().map(PlanSubjectDto::from).toList());
        }
    }

    record StudyPlanDto(UUID id, String code, String description, UUID studyLevelId, Integer semesterNumber,
            StudyPlanStatus status, List<UnitDto> units) {
        static StudyPlanDto from(StudyPlan p) {
            return new StudyPlanDto(p.getId(), p.getCode(), p.getDescription(), p.getStudyLevelId(),
                    p.getSemesterNumber(), p.getStatus(), p.getUnits().stream().map(UnitDto::from).toList());
        }
    }

    record AssessmentRequest(@NotNull AssessmentType assessmentType, @NotNull BigDecimal weightPercent,
            BigDecimal maxScore) {
    }

    record PlanSubjectRequest(@NotNull UUID subjectId, BigDecimal coefficient, Integer credits, Integer lectureHours,
            Integer tutorialHours, Integer labHours, Integer projectHours, List<@Valid AssessmentRequest> assessments) {
    }

    record UnitRequest(@NotBlank String code, @NotNull LocalizedText name, BigDecimal coefficient, Integer credits,
            @NotEmpty List<@Valid PlanSubjectRequest> subjects) {
    }

    record StudyPlanRequest(@NotBlank @Size(max = 80) String code, String description, @NotNull UUID studyLevelId,
            @NotNull Integer semesterNumber, @NotEmpty List<@Valid UnitRequest> units) {
    }

    record StudyPlanStatusRequest(@NotNull StudyPlanStatus status) {
    }

    record TeacherDto(UUID id, String employeeNumber, PersonInfo person, String gradeCode, LocalizedText grade,
            EmploymentType employmentType, LocalDate hireDate, Money hourlyRate, Integer statutoryHours,
            Integer maxWeeklyHours, boolean active) {
        static TeacherDto from(Teacher t) {
            return new TeacherDto(t.getId(), t.getEmployeeNumber(), t.getPerson(), t.getGrade().getCode(),
                    t.getGrade().getLabel(), t.getEmploymentType(), t.getHireDate(), t.getHourlyRate(),
                    t.getStatutoryHours(), t.getMaxWeeklyHours(), t.isActive());
        }
    }

    record TeacherRequest(@NotNull PersonInfo person, @Size(max = 40) String employeeNumber,
            @NotBlank String gradeCode, @NotNull EmploymentType employmentType, LocalDate hireDate, Money hourlyRate,
            Integer statutoryHours, Integer maxWeeklyHours) {
    }

    record AssignmentDto(UUID id, UUID studyPlanSubjectId, UUID subjectId, String subjectCode,
            LocalizedText subjectName, UUID schoolClassId, UUID academicYearId, SessionType sessionType,
            Integer plannedHours, UUID teacherId, String teacherName) {
        static AssignmentDto from(TeachingAssignment a) {
            Subject subject = a.getStudyPlanSubject().getSubject();
            Teacher teacher = a.getTeacher();
            return new AssignmentDto(a.getId(), a.getStudyPlanSubject().getId(), subject.getId(), subject.getCode(),
                    subject.getName(), a.getSchoolClassId(), a.getAcademicYearId(), a.getSessionType(),
                    a.getPlannedHours(), teacher == null ? null : teacher.getId(),
                    teacher == null ? null : teacher.getPerson().fullName());
        }
    }

    record AssignmentRequest(@NotNull UUID studyPlanSubjectId, @NotNull UUID schoolClassId,
            @NotNull SessionType sessionType, Integer plannedHours, UUID teacherId) {
    }

    record TeacherChangeRequest(UUID teacherId) {
    }

    record MaterialDto(UUID id, UUID assignmentId, LocalizedText subjectName, String title, String materialType,
            String description, UUID fileId, boolean visibleToStudents, Instant publishedAt, Instant createdAt) {
        static MaterialDto from(CourseMaterial m) {
            return new MaterialDto(m.getId(), m.getTeachingAssignment().getId(),
                    m.getTeachingAssignment().getStudyPlanSubject().getSubject().getName(), m.getTitle(),
                    m.getMaterialType().name(), m.getDescription(), m.getFileId(), m.isVisibleToStudents(),
                    m.getPublishedAt(), m.getCreatedAt());
        }
    }

    record JournalDto(UUID id, UUID sessionOccurrenceId, String content, Instant confirmedAt) {
        static JournalDto from(CourseJournalEntry e) {
            return new JournalDto(e.getId(), e.getSessionOccurrenceId(), e.getContent(), e.getConfirmedAt());
        }
    }

    record JournalRequest(@NotNull UUID sessionOccurrenceId, @NotBlank String content) {
    }

    record HomeworkDto(UUID id, UUID assignmentId, LocalizedText subjectName, String title, String description,
            LocalDate assignedDate, LocalDate dueDate) {
        static HomeworkDto from(Homework h) {
            return new HomeworkDto(h.getId(), h.getTeachingAssignment().getId(),
                    h.getTeachingAssignment().getStudyPlanSubject().getSubject().getName(), h.getTitle(),
                    h.getDescription(), h.getAssignedDate(), h.getDueDate());
        }
    }

    record HomeworkRequest(@NotBlank String title, String description, @NotNull LocalDate assignedDate,
            LocalDate dueDate, UUID sessionOccurrenceId) {
    }

    record MakeUpDto(UUID id, UUID assignmentId, LocalizedText subjectName, UUID schoolClassId, String teacherName,
            UUID originalOccurrenceId, LocalDate requestedDate, LocalTime startTime, LocalTime endTime, UUID roomId,
            String reason, MakeUpStatus status, Instant decidedAt) {
        static MakeUpDto from(MakeUpRequest r) {
            TeachingAssignment a = r.getTeachingAssignment();
            return new MakeUpDto(r.getId(), a.getId(), a.getStudyPlanSubject().getSubject().getName(),
                    a.getSchoolClassId(), r.getTeacher() == null ? null : r.getTeacher().getPerson().fullName(),
                    r.getOriginalOccurrenceId(), r.getRequestedDate(), r.getStartTime(), r.getEndTime(),
                    r.getRoomId(), r.getReason(), r.getStatus(), r.getDecidedAt());
        }
    }

    record MakeUpRequestBody(@NotNull UUID teachingAssignmentId, @NotNull UUID originalOccurrenceId,
            @NotNull LocalDate requestedDate, @NotNull LocalTime startTime, @NotNull LocalTime endTime, UUID roomId,
            @Size(max = 1000) String reason) {
    }

    record DecisionRequest(@NotNull MakeUpStatus status) {
    }

    record LinkDto(String url) {
    }
}
