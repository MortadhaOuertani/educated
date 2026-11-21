package tn.tekup.educated.pedagogie.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.tekup.educated.pedagogie.domain.MakeUpStatus;
import tn.tekup.educated.pedagogie.domain.TeachingAssignment;
import tn.tekup.educated.pedagogie.service.CatalogService;
import tn.tekup.educated.pedagogie.service.CatalogService.AssessmentData;
import tn.tekup.educated.pedagogie.service.CatalogService.PlanSubjectData;
import tn.tekup.educated.pedagogie.service.CatalogService.StudyPlanData;
import tn.tekup.educated.pedagogie.service.CatalogService.SubjectData;
import tn.tekup.educated.pedagogie.service.CatalogService.UnitData;
import tn.tekup.educated.pedagogie.service.MakeUpService;
import tn.tekup.educated.pedagogie.service.TeachingService;
import tn.tekup.educated.pedagogie.service.TeachingService.TeacherData;
import tn.tekup.educated.pedagogie.service.TeachingService.Workload;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.AssignmentDto;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.AssignmentRequest;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.DecisionRequest;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.MakeUpDto;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.StudyPlanDto;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.StudyPlanRequest;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.StudyPlanStatusRequest;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.SubjectDto;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.SubjectRequest;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.TeacherChangeRequest;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.TeacherDto;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.TeacherGradeDto;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.TeacherGradeRequest;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.TeacherRequest;

/** Back-office screens of the Pédagogie module. */
@RestController
@RequestMapping("/api/pedagogie")
class PedagogieController {

    private final CatalogService catalog;
    private final TeachingService teaching;
    private final MakeUpService makeUps;

    PedagogieController(CatalogService catalog, TeachingService teaching, MakeUpService makeUps) {
        this.catalog = catalog;
        this.teaching = teaching;
        this.makeUps = makeUps;
    }

    @GetMapping("/teacher-grades")
    @PreAuthorize("@perm.can('TeacherGrade', 'READ')")
    List<TeacherGradeDto> grades() {
        return catalog.grades().stream().map(TeacherGradeDto::from).toList();
    }

    @PostMapping("/teacher-grades")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('TeacherGrade', 'CREATE')")
    TeacherGradeDto createGrade(@Valid @RequestBody TeacherGradeRequest r) {
        return TeacherGradeDto.from(catalog.createGrade(r.code(), r.label()));
    }

    @GetMapping("/subjects")
    @PreAuthorize("@perm.can('Subject', 'READ')")
    List<SubjectDto> subjects() {
        return catalog.subjects().stream().map(SubjectDto::from).toList();
    }

    @PostMapping("/subjects")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('Subject', 'CREATE')")
    SubjectDto createSubject(@Valid @RequestBody SubjectRequest r) {
        return SubjectDto.from(catalog.createSubject(new SubjectData(r.code(), r.name(), r.description(),
                r.teachingDomainId(), r.defaultCoefficient(), r.defaultCredits())));
    }

    @GetMapping("/study-plans")
    @PreAuthorize("@perm.can('StudyPlan', 'READ')")
    @Transactional(readOnly = true)
    List<StudyPlanDto> studyPlans() {
        return catalog.studyPlans().stream().map(StudyPlanDto::from).toList();
    }

    @GetMapping("/study-plans/{id}")
    @PreAuthorize("@perm.can('StudyPlan', 'READ')")
    @Transactional(readOnly = true)
    StudyPlanDto studyPlan(@PathVariable UUID id) {
        return StudyPlanDto.from(catalog.studyPlan(id));
    }

    @PostMapping("/study-plans")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('StudyPlan', 'CREATE')")
    @Transactional
    StudyPlanDto createStudyPlan(@Valid @RequestBody StudyPlanRequest r) {
        List<UnitData> units = r.units().stream().map(unit -> new UnitData(unit.code(), unit.name(),
                unit.coefficient(), unit.credits(), unit.subjects().stream().map(subject -> new PlanSubjectData(
                        subject.subjectId(), subject.coefficient(), subject.credits(), subject.lectureHours(),
                        subject.tutorialHours(), subject.labHours(), subject.projectHours(),
                        subject.assessments() == null ? List.of() : subject.assessments().stream()
                                .map(a -> new AssessmentData(a.assessmentType(), a.weightPercent(), a.maxScore()))
                                .toList()))
                        .toList()))
                .toList();
        return StudyPlanDto.from(catalog.createStudyPlan(new StudyPlanData(r.code(), r.description(),
                r.studyLevelId(), r.semesterNumber(), units)));
    }

    @PutMapping("/study-plans/{id}/status")
    @PreAuthorize("@perm.can('StudyPlan', 'WRITE')")
    @Transactional
    StudyPlanDto changeStudyPlanStatus(@PathVariable UUID id, @Valid @RequestBody StudyPlanStatusRequest r) {
        return StudyPlanDto.from(catalog.changeStatus(id, r.status()));
    }

    @GetMapping("/teachers")
    @PreAuthorize("@perm.can('Teacher', 'READ')")
    @Transactional(readOnly = true)
    List<TeacherDto> teachers() {
        return teaching.teachers().stream().map(TeacherDto::from).toList();
    }

    @GetMapping("/teachers/{id}")
    @PreAuthorize("@perm.can('Teacher', 'READ')")
    @Transactional(readOnly = true)
    TeacherDto teacher(@PathVariable UUID id) {
        return TeacherDto.from(teaching.teacher(id));
    }

    @PostMapping("/teachers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('Teacher', 'CREATE')")
    @Transactional
    TeacherDto createTeacher(@Valid @RequestBody TeacherRequest r) {
        return TeacherDto.from(teaching.createTeacher(new TeacherData(r.person(), r.employeeNumber(), r.gradeCode(),
                r.employmentType(), r.hireDate(), r.hourlyRate(), r.statutoryHours(), r.maxWeeklyHours())));
    }

    @GetMapping("/teachers/{id}/workload")
    @PreAuthorize("@perm.can('TeacherWorkload', 'READ')")
    Workload workload(@PathVariable UUID id, @RequestParam UUID academicYearId) {
        return teaching.workload(id, academicYearId);
    }

    @GetMapping("/assignments")
    @PreAuthorize("@perm.can('TeachingAssignment', 'READ')")
    @Transactional(readOnly = true)
    List<AssignmentDto> assignments(@RequestParam(required = false) UUID schoolClassId,
            @RequestParam(required = false) UUID teacherId) {
        List<TeachingAssignment> found = schoolClassId != null ? teaching.assignmentsOfClass(schoolClassId)
                : teacherId != null ? teaching.assignmentsOfTeacher(teacherId) : List.of();
        return found.stream().map(AssignmentDto::from).toList();
    }

    @PostMapping("/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('TeachingAssignment', 'CREATE')")
    @Transactional
    AssignmentDto createAssignment(@Valid @RequestBody AssignmentRequest r) {
        return AssignmentDto.from(teaching.createAssignment(r.studyPlanSubjectId(), r.schoolClassId(),
                r.sessionType(), r.plannedHours(), r.teacherId()));
    }

    @PutMapping("/assignments/{id}/teacher")
    @PreAuthorize("@perm.can('TeachingAssignment', 'WRITE')")
    @Transactional
    AssignmentDto changeTeacher(@PathVariable UUID id, @RequestBody TeacherChangeRequest r) {
        return AssignmentDto.from(teaching.changeTeacher(id, r.teacherId()));
    }

    @GetMapping("/make-up-requests")
    @PreAuthorize("@perm.can('MakeUpRequest', 'READ')")
    @Transactional(readOnly = true)
    List<MakeUpDto> makeUpRequests(@RequestParam(required = false) MakeUpStatus status) {
        return makeUps.list(status).stream().map(MakeUpDto::from).toList();
    }

    @PatchMapping("/make-up-requests/{id}")
    @PreAuthorize("@perm.can('MakeUpRequest', 'WRITE')")
    @Transactional
    MakeUpDto decide(@PathVariable UUID id, @Valid @RequestBody DecisionRequest r) {
        return MakeUpDto.from(makeUps.decide(id, r.status()));
    }
}
