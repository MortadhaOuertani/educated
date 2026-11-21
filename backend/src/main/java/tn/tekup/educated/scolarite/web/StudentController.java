package tn.tekup.educated.scolarite.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.tekup.educated.scolarite.service.DocumentRequestService;
import tn.tekup.educated.scolarite.service.EnrollmentService;
import tn.tekup.educated.scolarite.service.SanctionService;
import tn.tekup.educated.scolarite.service.StudentService;
import tn.tekup.educated.scolarite.service.StudentService.GuardianData;
import tn.tekup.educated.scolarite.service.StudentService.StudentData;
import tn.tekup.educated.scolarite.web.ScolariteDtos.ChangeClassRequest;
import tn.tekup.educated.scolarite.web.ScolariteDtos.DocumentRequestDto;
import tn.tekup.educated.scolarite.web.ScolariteDtos.EnrollRequest;
import tn.tekup.educated.scolarite.web.ScolariteDtos.EnrollmentDto;
import tn.tekup.educated.scolarite.web.ScolariteDtos.GuardianLinkDto;
import tn.tekup.educated.scolarite.web.ScolariteDtos.KinshipDto;
import tn.tekup.educated.scolarite.web.ScolariteDtos.NewGuardianRequest;
import tn.tekup.educated.scolarite.web.ScolariteDtos.SanctionDto;
import tn.tekup.educated.scolarite.web.ScolariteDtos.SanctionRequest;
import tn.tekup.educated.scolarite.web.ScolariteDtos.StatusRequest;
import tn.tekup.educated.scolarite.web.ScolariteDtos.StudentDto;
import tn.tekup.educated.scolarite.web.ScolariteDtos.StudentRequest;
import tn.tekup.educated.shared.web.PageResponse;

/** Back-office screens of the Scolarité module (Apprenants, Responsables, Inscriptions, Sanctions). */
@RestController
@RequestMapping("/api/scolarite")
class StudentController {

    private final StudentService students;
    private final EnrollmentService enrollments;
    private final SanctionService sanctions;
    private final DocumentRequestService documentRequests;

    StudentController(StudentService students, EnrollmentService enrollments, SanctionService sanctions,
            DocumentRequestService documentRequests) {
        this.students = students;
        this.enrollments = enrollments;
        this.sanctions = sanctions;
        this.documentRequests = documentRequests;
    }

    @GetMapping("/students")
    @PreAuthorize("@perm.can('Student', 'READ')")
    PageResponse<StudentDto> search(@RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("person.lastName", "person.firstName"));
        return PageResponse.of(students.search(search, pageable), StudentDto::from);
    }

    @GetMapping("/students/{id}")
    @PreAuthorize("@perm.can('Student', 'READ')")
    StudentDto get(@PathVariable UUID id) {
        return StudentDto.from(students.get(id));
    }

    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('Student', 'CREATE')")
    StudentDto create(@Valid @RequestBody StudentRequest r) {
        return StudentDto.from(students.create(data(r)));
    }

    @PutMapping("/students/{id}")
    @PreAuthorize("@perm.can('Student', 'WRITE')")
    StudentDto update(@PathVariable UUID id, @Valid @RequestBody StudentRequest r) {
        return StudentDto.from(students.update(id, data(r)));
    }

    @PutMapping("/students/{id}/status")
    @PreAuthorize("@perm.can('Student', 'WRITE')")
    StudentDto changeStatus(@PathVariable UUID id, @Valid @RequestBody StatusRequest r) {
        return StudentDto.from(students.changeStatus(id, r.status()));
    }

    @GetMapping("/students/{id}/guardians")
    @PreAuthorize("@perm.can('StudentGuardian', 'READ')")
    List<GuardianLinkDto> guardians(@PathVariable UUID id) {
        return students.guardiansOf(id).stream().map(GuardianLinkDto::from).toList();
    }

    @PostMapping("/students/{id}/guardians")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('Guardian', 'CREATE')")
    @Transactional
    GuardianLinkDto addGuardian(@PathVariable UUID id, @Valid @RequestBody NewGuardianRequest r) {
        var data = new GuardianData(r.person(), r.profession(), r.employer(), r.workPhone(), r.preferredLanguage(),
                r.smsOptIn());
        return GuardianLinkDto.from(students.addGuardian(id, data, r.kinshipCode(), r.primaryContact(),
                r.financialResponsible()));
    }

    @GetMapping("/kinships")
    @PreAuthorize("@perm.can('Kinship', 'READ')")
    List<KinshipDto> kinships() {
        return students.kinships().stream().map(KinshipDto::from).toList();
    }

    @GetMapping("/students/{id}/enrollments")
    @PreAuthorize("@perm.can('Enrollment', 'READ')")
    @Transactional(readOnly = true)
    List<EnrollmentDto> enrollments(@PathVariable UUID id) {
        return enrollments.enrollmentsOf(id).stream().map(EnrollmentDto::from).toList();
    }

    @PostMapping("/students/{id}/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('Enrollment', 'CREATE')")
    @Transactional
    EnrollmentDto enroll(@PathVariable UUID id, @Valid @RequestBody EnrollRequest r) {
        return EnrollmentDto.from(
                enrollments.enroll(id, r.schoolClassId(), r.enrollmentType(), r.enrollmentDate(), r.repeating()));
    }

    @PostMapping("/enrollments/{enrollmentId}/change-class")
    @PreAuthorize("@perm.can('ClassAssignment', 'CREATE')")
    @Transactional
    EnrollmentDto changeClass(@PathVariable UUID enrollmentId, @Valid @RequestBody ChangeClassRequest r) {
        return EnrollmentDto.from(enrollments.changeClass(enrollmentId, r.schoolClassId(), r.date()));
    }

    @GetMapping("/students/{id}/sanctions")
    @PreAuthorize("@perm.can('Sanction', 'READ')")
    List<SanctionDto> sanctions(@PathVariable UUID id) {
        return sanctions.sanctionsOf(id).stream().map(SanctionDto::from).toList();
    }

    @PostMapping("/students/{id}/sanctions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('Sanction', 'CREATE')")
    SanctionDto issueSanction(@PathVariable UUID id, @Valid @RequestBody SanctionRequest r) {
        return SanctionDto.from(sanctions.issue(id, r.sanctionType(), r.reason(), r.incidentDate(), r.startDate(),
                r.endDate(), r.notifyGuardians()));
    }

    @GetMapping("/students/{id}/document-requests")
    @PreAuthorize("@perm.can('DocumentRequest', 'READ')")
    @Transactional(readOnly = true)
    List<DocumentRequestDto> documentRequests(@PathVariable UUID id) {
        return documentRequests.requestsOf(id).stream().map(DocumentRequestDto::from).toList();
    }

    private static StudentData data(StudentRequest r) {
        return new StudentData(r.person(), r.matricule(), r.admissionDate(), r.baccalaureateSection(),
                r.baccalaureateAverage(), r.baccalaureateYear(), r.previousInstitution());
    }
}
