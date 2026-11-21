package tn.tekup.educated.stages.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.tekup.educated.shared.domain.Address;
import tn.tekup.educated.stages.domain.InternshipStatus;
import tn.tekup.educated.stages.domain.JuryRole;
import tn.tekup.educated.stages.service.DefenseService;
import tn.tekup.educated.stages.service.DefenseService.MemberData;
import tn.tekup.educated.stages.service.DefenseService.SessionData;
import tn.tekup.educated.stages.service.InternshipService;
import tn.tekup.educated.stages.service.InternshipService.CompanyData;
import tn.tekup.educated.stages.web.StagesViews.CompanyDto;
import tn.tekup.educated.stages.web.StagesViews.DefenseDto;
import tn.tekup.educated.stages.web.StagesViews.InternshipDto;
import tn.tekup.educated.stages.web.StagesViews.JuryDto;
import tn.tekup.educated.stages.web.StagesViews.NatureDto;
import tn.tekup.educated.stages.web.StagesViews.SessionDto;

/** Back-office screens of the Stages module (internship office). */
@RestController
@RequestMapping("/api/stages")
class StagesController {

    record CompanyRequest(@NotBlank String name, String sector, String email, String phone, Address address,
            boolean partner) {
    }

    record ApproveRequest(@NotNull UUID academicSupervisorId) {
    }

    record RefuseRequest(@NotBlank String reason) {
    }

    record SessionRequest(@NotBlank String name, @NotNull UUID academicYearId, @NotNull LocalDate startDate,
            @NotNull LocalDate endDate, LocalDate requestsOpenFrom, LocalDate requestsOpenTo) {
    }

    record MemberRequest(UUID teacherId, String externalName, @NotNull JuryRole role) {
    }

    record JuryRequest(@NotNull UUID defenseSessionId, @NotBlank String code, @NotNull LocalDate juryDate,
            UUID roomId, @NotEmpty List<@Valid MemberRequest> members) {
    }

    record DefenseRequest(@NotNull UUID internshipId, @NotNull UUID juryId, @NotNull LocalTime startTime,
            @NotNull LocalTime endTime) {
    }

    record GradeRequest(@NotNull BigDecimal grade) {
    }

    private final InternshipService internships;
    private final DefenseService defenses;
    private final StagesViews views;

    StagesController(InternshipService internships, DefenseService defenses, StagesViews views) {
        this.internships = internships;
        this.defenses = defenses;
        this.views = views;
    }

    @GetMapping("/natures")
    @PreAuthorize("@perm.can('InternshipNature', 'READ')")
    List<NatureDto> natures() {
        return internships.natures().stream().map(NatureDto::from).toList();
    }

    @GetMapping("/companies")
    @PreAuthorize("@perm.can('Company', 'READ')")
    List<CompanyDto> companies() {
        return internships.companies().stream().map(CompanyDto::from).toList();
    }

    @PostMapping("/companies")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('Company', 'CREATE')")
    CompanyDto createCompany(@Valid @RequestBody CompanyRequest r) {
        return CompanyDto.from(internships.createCompany(
                new CompanyData(r.name(), r.sector(), r.email(), r.phone(), r.address()), r.partner()));
    }

    @GetMapping("/internships")
    @PreAuthorize("@perm.can('Internship', 'READ')")
    @Transactional(readOnly = true)
    List<InternshipDto> internships(@RequestParam(required = false) InternshipStatus status) {
        return internships.list(status).stream().map(i -> views.internship(i, false)).toList();
    }

    @GetMapping("/internships/{id}")
    @PreAuthorize("@perm.can('Internship', 'READ')")
    @Transactional(readOnly = true)
    InternshipDto internship(@PathVariable UUID id) {
        return views.internship(internships.internship(id), false);
    }

    @PostMapping("/internships/{id}/approve")
    @PreAuthorize("@perm.can('Internship', 'WRITE')")
    @Transactional
    InternshipDto approve(@PathVariable UUID id, @Valid @RequestBody ApproveRequest r) {
        return views.internship(internships.approve(id, r.academicSupervisorId()), false);
    }

    @PostMapping("/internships/{id}/refuse")
    @PreAuthorize("@perm.can('Internship', 'WRITE')")
    @Transactional
    InternshipDto refuse(@PathVariable UUID id, @Valid @RequestBody RefuseRequest r) {
        return views.internship(internships.refuse(id, r.reason()), false);
    }

    @PostMapping("/internships/{id}/authorize-defense")
    @PreAuthorize("@perm.can('Internship', 'WRITE')")
    @Transactional
    InternshipDto authorizeDefense(@PathVariable UUID id) {
        return views.internship(internships.authorizeDefense(id), false);
    }

    @GetMapping("/defense-sessions")
    @PreAuthorize("@perm.can('DefenseSession', 'READ')")
    List<SessionDto> sessions() {
        return defenses.sessions().stream().map(SessionDto::from).toList();
    }

    @PostMapping("/defense-sessions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('DefenseSession', 'CREATE')")
    SessionDto createSession(@Valid @RequestBody SessionRequest r) {
        return SessionDto.from(defenses.createSession(new SessionData(r.name(), r.academicYearId(), r.startDate(),
                r.endDate(), r.requestsOpenFrom(), r.requestsOpenTo())));
    }

    @GetMapping("/juries")
    @PreAuthorize("@perm.can('Jury', 'READ')")
    @Transactional(readOnly = true)
    List<JuryDto> juries(@RequestParam UUID defenseSessionId) {
        return defenses.juriesOf(defenseSessionId).stream().map(views::jury).toList();
    }

    @PostMapping("/juries")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('Jury', 'CREATE')")
    @Transactional
    JuryDto createJury(@Valid @RequestBody JuryRequest r) {
        List<MemberData> members = r.members().stream()
                .map(m -> new MemberData(m.teacherId(), m.externalName(), m.role()))
                .toList();
        return views.jury(defenses.createJury(r.defenseSessionId(), r.code(), r.juryDate(), r.roomId(), members));
    }

    @PostMapping("/defenses")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('Defense', 'CREATE')")
    @Transactional
    DefenseDto schedule(@Valid @RequestBody DefenseRequest r) {
        return views.defense(defenses.schedule(r.internshipId(), r.juryId(), r.startTime(), r.endTime()), false);
    }

    @PatchMapping("/defenses/{id}/grade")
    @PreAuthorize("@perm.can('Defense', 'WRITE')")
    @Transactional
    DefenseDto grade(@PathVariable UUID id, @Valid @RequestBody GradeRequest r) {
        return views.defense(defenses.recordGrade(id, r.grade()), false);
    }

    @PostMapping("/defenses/{id}/publish")
    @PreAuthorize("@perm.can('Defense', 'WRITE')")
    @Transactional
    DefenseDto publish(@PathVariable UUID id) {
        return views.defense(defenses.publish(id), false);
    }
}
