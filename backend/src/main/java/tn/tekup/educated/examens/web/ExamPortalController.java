package tn.tekup.educated.examens.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tn.tekup.educated.examens.domain.GradeReason;
import tn.tekup.educated.examens.service.ExamService;
import tn.tekup.educated.examens.service.GradeService;
import tn.tekup.educated.examens.service.GradeService.GradeData;
import tn.tekup.educated.examens.service.ReportCardService;
import tn.tekup.educated.examens.web.ExamViews.ExamDto;
import tn.tekup.educated.examens.web.ExamViews.GridDto;
import tn.tekup.educated.examens.web.ExamViews.InvigilationDto;
import tn.tekup.educated.examens.web.ExamViews.ReportCardDto;
import tn.tekup.educated.examens.web.ExamViews.StudentGradeDto;
import tn.tekup.educated.scolarite.api.StudentDirectory;
import tn.tekup.educated.shared.domain.AssessmentType;
import tn.tekup.educated.shared.domain.PersonType;
import tn.tekup.educated.shared.security.CurrentUserProvider;

/** Exams in the portals: teachers enter grades and see their invigilations, students see exams and results. */
@RestController
class ExamPortalController {

    record OpenGridRequest(@NotNull UUID teachingAssignmentId, @NotNull UUID academicPeriodId,
            @NotNull AssessmentType assessmentType) {
    }

    record GradeRequest(@NotNull UUID studentId, BigDecimal score, GradeReason reason, String comment) {
    }

    record GradesRequest(@NotNull List<@Valid GradeRequest> grades) {
    }

    private final CurrentUserProvider currentUserProvider;
    private final ExamService exams;
    private final GradeService grades;
    private final ReportCardService reportCards;
    private final StudentDirectory students;
    private final ExamViews views;

    ExamPortalController(CurrentUserProvider currentUserProvider, ExamService exams, GradeService grades,
            ReportCardService reportCards, StudentDirectory students, ExamViews views) {
        this.currentUserProvider = currentUserProvider;
        this.exams = exams;
        this.grades = grades;
        this.reportCards = reportCards;
        this.students = students;
        this.views = views;
    }

    @GetMapping("/api/teacher/invigilations")
    @Transactional(readOnly = true)
    List<InvigilationDto> invigilations() {
        return exams.invigilationsOf(teacherId()).stream().map(views::invigilation).toList();
    }

    /** Opens the grid of one assessment (e.g. the TEST of Architecture logicielle in semester 1). */
    @PostMapping("/api/teacher/grade-grids")
    @Transactional
    GridDto openGrid(@Valid @RequestBody OpenGridRequest r) {
        return views.grid(grades.openGrid(r.teachingAssignmentId(), r.academicPeriodId(), r.assessmentType(),
                teacherId()));
    }

    @GetMapping("/api/teacher/grade-grids/{id}")
    @Transactional(readOnly = true)
    GridDto grid(@PathVariable UUID id) {
        return views.grid(grades.grid(id, teacherId()));
    }

    @PutMapping("/api/teacher/grade-grids/{id}/grades")
    @Transactional
    GridDto saveGrades(@PathVariable UUID id, @Valid @RequestBody GradesRequest r) {
        List<GradeData> data = r.grades().stream()
                .map(g -> new GradeData(g.studentId(), g.score(), g.reason(), g.comment()))
                .toList();
        return views.grid(grades.saveGrades(id, teacherId(), data));
    }

    @PostMapping("/api/teacher/grade-grids/{id}/confirm")
    @Transactional
    GridDto confirm(@PathVariable UUID id) {
        return views.grid(grades.confirm(id, teacherId()));
    }

    @GetMapping("/api/student/exams")
    @Transactional(readOnly = true)
    List<ExamDto> myExams() {
        return students.currentClassId(studentId()).map(exams::publishedExamsOf).orElse(List.of()).stream()
                .map(views::exam).toList();
    }

    @GetMapping("/api/student/grades")
    @Transactional(readOnly = true)
    List<StudentGradeDto> myGrades() {
        return grades.publishedGradesOf(studentId()).stream().map(views::studentGrade).toList();
    }

    @GetMapping("/api/student/report-cards")
    @Transactional(readOnly = true)
    List<ReportCardDto> myReportCards() {
        return reportCards.publishedReportCardsOf(studentId()).stream().map(views::reportCard).toList();
    }

    private UUID teacherId() {
        return currentUserProvider.requirePerson(PersonType.TEACHER);
    }

    private UUID studentId() {
        return currentUserProvider.requirePerson(PersonType.STUDENT);
    }
}
