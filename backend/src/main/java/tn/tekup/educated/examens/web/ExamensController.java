package tn.tekup.educated.examens.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.tekup.educated.examens.domain.ExamSessionType;
import tn.tekup.educated.examens.service.ExamService;
import tn.tekup.educated.examens.service.ExamService.ExamData;
import tn.tekup.educated.examens.service.ExamService.InvigilatorData;
import tn.tekup.educated.examens.service.ExamService.RoomData;
import tn.tekup.educated.examens.service.GradeService;
import tn.tekup.educated.examens.service.ReportCardService;
import tn.tekup.educated.examens.web.ExamViews.ExamDto;
import tn.tekup.educated.examens.web.ExamViews.GridDto;
import tn.tekup.educated.examens.web.ExamViews.ReportCardDto;
import tn.tekup.educated.examens.web.ExamViews.SessionDto;
import tn.tekup.educated.shared.domain.AssessmentType;

/** Back-office screens of the Examens et notes module. */
@RestController
@RequestMapping("/api/examens")
class ExamensController {

    record SessionRequest(@NotBlank String name, @NotNull ExamSessionType sessionType, @NotNull UUID academicPeriodId,
            @NotNull LocalDate startDate, @NotNull LocalDate endDate) {
    }

    record ExamRequest(@NotNull UUID studyPlanSubjectId, @NotNull AssessmentType assessmentType,
            @NotNull LocalDate examDate, @NotNull LocalTime startTime, @NotNull LocalTime endTime,
            @NotEmpty List<UUID> schoolClassIds, List<RoomData> rooms, List<InvigilatorData> invigilators) {
    }

    record ClassPeriodRequest(@NotNull UUID schoolClassId, @NotNull UUID academicPeriodId) {
    }

    private final ExamService exams;
    private final GradeService grades;
    private final ReportCardService reportCards;
    private final ExamViews views;

    ExamensController(ExamService exams, GradeService grades, ReportCardService reportCards, ExamViews views) {
        this.exams = exams;
        this.grades = grades;
        this.reportCards = reportCards;
        this.views = views;
    }

    @GetMapping("/exam-sessions")
    @PreAuthorize("@perm.can('ExamSession', 'READ')")
    List<SessionDto> sessions() {
        return exams.sessions().stream().map(SessionDto::from).toList();
    }

    @PostMapping("/exam-sessions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('ExamSession', 'CREATE')")
    SessionDto createSession(@Valid @RequestBody SessionRequest r) {
        return SessionDto.from(exams.createSession(r.name(), r.sessionType(), r.academicPeriodId(), r.startDate(),
                r.endDate()));
    }

    @GetMapping("/exam-sessions/{id}/exams")
    @PreAuthorize("@perm.can('Exam', 'READ')")
    @Transactional(readOnly = true)
    List<ExamDto> examsOf(@PathVariable UUID id) {
        return exams.examsOf(id).stream().map(views::exam).toList();
    }

    @PostMapping("/exam-sessions/{id}/exams")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('Exam', 'CREATE')")
    @Transactional
    ExamDto createExam(@PathVariable UUID id, @Valid @RequestBody ExamRequest r) {
        return views.exam(exams.createExam(id, new ExamData(r.studyPlanSubjectId(), r.assessmentType(), r.examDate(),
                r.startTime(), r.endTime(), r.schoolClassIds(), r.rooms(), r.invigilators())));
    }

    @PostMapping("/exam-sessions/{id}/publish")
    @PreAuthorize("@perm.can('ExamSession', 'WRITE')")
    @Transactional
    SessionDto publishSession(@PathVariable UUID id) {
        return SessionDto.from(exams.publish(id));
    }

    @GetMapping("/grade-grids")
    @PreAuthorize("@perm.can('GradeGrid', 'READ')")
    @Transactional(readOnly = true)
    List<GridDto> grids(@RequestParam UUID schoolClassId, @RequestParam UUID academicPeriodId) {
        return grades.gridsOf(schoolClassId, academicPeriodId).stream().map(views::grid).toList();
    }

    @PostMapping("/grade-grids/{id}/publish")
    @PreAuthorize("@perm.can('GradeGrid', 'WRITE')")
    @Transactional
    GridDto publishGrid(@PathVariable UUID id) {
        return views.grid(grades.publish(id));
    }

    @PostMapping("/report-cards/generate")
    @PreAuthorize("@perm.can('ReportCard', 'CREATE')")
    @Transactional
    List<ReportCardDto> generate(@Valid @RequestBody ClassPeriodRequest r) {
        return reportCards.generate(r.schoolClassId(), r.academicPeriodId()).stream().map(views::reportCard).toList();
    }

    @GetMapping("/report-cards")
    @PreAuthorize("@perm.can('ReportCard', 'READ')")
    @Transactional(readOnly = true)
    List<ReportCardDto> reportCards(@RequestParam UUID schoolClassId, @RequestParam UUID academicPeriodId) {
        return reportCards.reportCardsOf(schoolClassId, academicPeriodId).stream().map(views::reportCard).toList();
    }

    @PostMapping("/report-cards/publish")
    @PreAuthorize("@perm.can('ReportCard', 'WRITE')")
    @Transactional
    List<ReportCardDto> publishReportCards(@Valid @RequestBody ClassPeriodRequest r) {
        return reportCards.publish(r.schoolClassId(), r.academicPeriodId()).stream().map(views::reportCard).toList();
    }
}
