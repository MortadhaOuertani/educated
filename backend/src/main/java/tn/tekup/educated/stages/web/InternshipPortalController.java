package tn.tekup.educated.stages.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tn.tekup.educated.shared.domain.PersonType;
import tn.tekup.educated.shared.files.FileStorage;
import tn.tekup.educated.shared.security.CurrentUserProvider;
import tn.tekup.educated.shared.web.NotFoundException;
import tn.tekup.educated.stages.domain.Internship;
import tn.tekup.educated.stages.domain.InternshipDocumentType;
import tn.tekup.educated.stages.service.DefenseService;
import tn.tekup.educated.stages.service.InternshipService;
import tn.tekup.educated.stages.service.InternshipService.CompanyData;
import tn.tekup.educated.stages.service.InternshipService.InternshipData;
import tn.tekup.educated.stages.service.InternshipService.SupervisorData;
import tn.tekup.educated.stages.web.StagesViews.CompanyDto;
import tn.tekup.educated.stages.web.StagesViews.InternshipDto;
import tn.tekup.educated.stages.web.StagesViews.JuryDto;
import tn.tekup.educated.stages.web.StagesViews.NatureDto;
import tn.tekup.educated.stages.web.StagesViews.SessionDto;

/** Internships in the portals: students declare and follow theirs, teachers follow the ones they supervise. */
@RestController
class InternshipPortalController {

    record InternshipOptions(List<NatureDto> natures, List<CompanyDto> companies, List<SessionDto> defenseSessions) {
    }

    record DeclareRequest(@NotNull UUID natureId, UUID companyId, CompanyData newCompany, SupervisorData supervisor,
            @NotBlank String title, String objectives, @NotNull LocalDate startDate, @NotNull LocalDate endDate) {
    }

    record DefenseRequestBody(@NotNull UUID defenseSessionId) {
    }

    private final CurrentUserProvider currentUserProvider;
    private final InternshipService internships;
    private final DefenseService defenses;
    private final StagesViews views;
    private final FileStorage fileStorage;

    InternshipPortalController(CurrentUserProvider currentUserProvider, InternshipService internships,
            DefenseService defenses, StagesViews views, FileStorage fileStorage) {
        this.currentUserProvider = currentUserProvider;
        this.internships = internships;
        this.defenses = defenses;
        this.views = views;
        this.fileStorage = fileStorage;
    }

    /** What the student can pick from when declaring an internship or requesting a defense. */
    @GetMapping("/api/student/internship-options")
    InternshipOptions options() {
        studentId();
        return new InternshipOptions(internships.natures().stream().map(NatureDto::from).toList(),
                internships.companies().stream().map(CompanyDto::from).toList(),
                defenses.sessions().stream().map(SessionDto::from).toList());
    }

    @GetMapping("/api/student/internships")
    @Transactional(readOnly = true)
    List<InternshipDto> myInternships() {
        return internships.internshipsOf(studentId()).stream().map(i -> views.internship(i, true)).toList();
    }

    @PostMapping("/api/student/internships")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    InternshipDto declare(@Valid @RequestBody DeclareRequest r) {
        return views.internship(internships.declare(studentId(), new InternshipData(r.natureId(), r.companyId(),
                r.newCompany(), r.supervisor(), r.title(), r.objectives(), r.startDate(), r.endDate())), true);
    }

    /** Dépôt du rapport, de l'attestation ou de la convention (PDF/JPG/PNG, 20 MB). */
    @PostMapping(value = "/api/student/internships/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    InternshipDto addDocument(@PathVariable UUID id, @RequestParam InternshipDocumentType documentType,
            @RequestParam MultipartFile file) {
        Internship internship = myInternship(id);
        UUID fileId = fileStorage.store(file, FileStorage.MAX_MESSAGE_ATTACHMENT_BYTES, FileStorage.DOCUMENT_TYPES);
        return views.internship(internships.addDocument(internship, documentType, fileId), true);
    }

    @PostMapping("/api/student/internships/{id}/defense-request")
    @Transactional
    InternshipDto requestDefense(@PathVariable UUID id, @Valid @RequestBody DefenseRequestBody r) {
        return views.internship(internships.requestDefense(myInternship(id), r.defenseSessionId()), true);
    }

    @GetMapping("/api/teacher/internships")
    @Transactional(readOnly = true)
    List<InternshipDto> supervised() {
        return internships.supervisedBy(teacherId()).stream().map(i -> views.internship(i, false)).toList();
    }

    @PostMapping("/api/teacher/internships/{id}/authorize-defense")
    @Transactional
    InternshipDto authorize(@PathVariable UUID id) {
        return views.internship(internships.authorizeBySupervisor(internships.internship(id), teacherId()), false);
    }

    /** Juries the teacher sits on, with the planned defenses. */
    @GetMapping("/api/teacher/juries")
    @Transactional(readOnly = true)
    List<JuryDto> myJuries() {
        return defenses.juriesWithMember(teacherId()).stream().map(views::jury).toList();
    }

    private Internship myInternship(UUID id) {
        Internship internship = internships.internship(id);
        if (!internship.getStudentId().equals(studentId())) {
            throw new NotFoundException("Internship", id);
        }
        return internship;
    }

    private UUID studentId() {
        return currentUserProvider.requirePerson(PersonType.STUDENT);
    }

    private UUID teacherId() {
        return currentUserProvider.requirePerson(PersonType.TEACHER);
    }
}
