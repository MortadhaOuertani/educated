package tn.tekup.educated.parametrage.web;

import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
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
import tn.tekup.educated.parametrage.api.ParametrageDirectory;
import tn.tekup.educated.parametrage.api.RoomInfo;
import tn.tekup.educated.parametrage.domain.EstablishmentRepository;
import tn.tekup.educated.parametrage.domain.FeatureToggleRepository;
import tn.tekup.educated.parametrage.domain.Portal;
import tn.tekup.educated.parametrage.service.AcademicYearService;
import tn.tekup.educated.parametrage.service.StructureService;
import tn.tekup.educated.parametrage.web.ParametrageDtos.AcademicYearDto;
import tn.tekup.educated.parametrage.web.ParametrageDtos.CycleDto;
import tn.tekup.educated.parametrage.web.ParametrageDtos.EstablishmentDto;
import tn.tekup.educated.parametrage.web.ParametrageDtos.FeatureToggleDto;
import tn.tekup.educated.parametrage.web.ParametrageDtos.NewAcademicYearRequest;
import tn.tekup.educated.parametrage.web.ParametrageDtos.NewCycleRequest;
import tn.tekup.educated.parametrage.web.ParametrageDtos.NewProgramRequest;
import tn.tekup.educated.parametrage.web.ParametrageDtos.NewStudyLevelRequest;
import tn.tekup.educated.parametrage.web.ParametrageDtos.ProgramDto;
import tn.tekup.educated.parametrage.web.ParametrageDtos.SchoolClassDto;
import tn.tekup.educated.parametrage.web.ParametrageDtos.SchoolClassRequest;
import tn.tekup.educated.parametrage.web.ParametrageDtos.StudyLevelDto;
import tn.tekup.educated.shared.security.CurrentUserProvider;
import tn.tekup.educated.shared.security.TenantAccess;
import tn.tekup.educated.shared.web.BusinessRuleException;

@RestController
@RequestMapping("/api/parametrage")
class ParametrageController {

    private final AcademicYearService academicYearService;
    private final StructureService structureService;
    private final EstablishmentRepository establishments;
    private final FeatureToggleRepository featureToggles;
    private final CurrentUserProvider currentUserProvider;
    private final TenantAccess tenantAccess;
    private final ParametrageDirectory directory;

    ParametrageController(AcademicYearService academicYearService, StructureService structureService,
            EstablishmentRepository establishments, FeatureToggleRepository featureToggles,
            CurrentUserProvider currentUserProvider, TenantAccess tenantAccess, ParametrageDirectory directory) {
        this.academicYearService = academicYearService;
        this.structureService = structureService;
        this.establishments = establishments;
        this.featureToggles = featureToggles;
        this.currentUserProvider = currentUserProvider;
        this.tenantAccess = tenantAccess;
        this.directory = directory;
    }

    /** Establishments the signed-in user may switch to (header selector). */
    @GetMapping("/establishments")
    List<EstablishmentDto> myEstablishments() {
        var ids = tenantAccess.accessibleEstablishments(currentUserProvider.require());
        return establishments.findAllById(ids).stream()
                .sorted(Comparator.comparing(e -> e.getCode()))
                .map(EstablishmentDto::from)
                .toList();
    }

    @GetMapping("/academic-years")
    @PreAuthorize("@perm.can('AcademicYear', 'READ')")
    @Transactional(readOnly = true)
    List<AcademicYearDto> academicYears() {
        return academicYearService.list().stream().map(AcademicYearDto::from).toList();
    }

    @PostMapping("/academic-years")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('AcademicYear', 'CREATE')")
    @Transactional
    AcademicYearDto createAcademicYear(@Valid @RequestBody NewAcademicYearRequest request) {
        var periods = request.periods() == null ? List.<AcademicYearService.NewPeriod>of()
                : request.periods().stream()
                        .map(p -> new AcademicYearService.NewPeriod(p.code(), p.name(), p.startDate(), p.endDate()))
                        .toList();
        return AcademicYearDto.from(
                academicYearService.create(request.code(), request.startDate(), request.endDate(), periods));
    }

    @PostMapping("/academic-years/{id}/open")
    @PreAuthorize("@perm.can('AcademicYear', 'WRITE')")
    @Transactional
    AcademicYearDto openAcademicYear(@PathVariable UUID id) {
        return AcademicYearDto.from(academicYearService.open(id));
    }

    @GetMapping("/cycles")
    @PreAuthorize("@perm.can('Cycle', 'READ')")
    List<CycleDto> cycles() {
        return structureService.cycles().stream().map(CycleDto::from).toList();
    }

    @PostMapping("/cycles")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('Cycle', 'CREATE')")
    CycleDto createCycle(@Valid @RequestBody NewCycleRequest request) {
        return CycleDto.from(structureService.createCycle(request.code(), request.name(), request.durationYears()));
    }

    @GetMapping("/programs")
    @PreAuthorize("@perm.can('Program', 'READ')")
    List<ProgramDto> programs() {
        return structureService.programs().stream().map(ProgramDto::from).toList();
    }

    @PostMapping("/programs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('Program', 'CREATE')")
    ProgramDto createProgram(@Valid @RequestBody NewProgramRequest request) {
        return ProgramDto.from(structureService.createProgram(request.code(), request.name(), request.diplomaTitle()));
    }

    @GetMapping("/study-levels")
    @PreAuthorize("@perm.can('StudyLevel', 'READ')")
    List<StudyLevelDto> studyLevels() {
        return structureService.studyLevels().stream().map(StudyLevelDto::from).toList();
    }

    @PostMapping("/study-levels")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('StudyLevel', 'CREATE')")
    StudyLevelDto createStudyLevel(@Valid @RequestBody NewStudyLevelRequest r) {
        return StudyLevelDto.from(structureService.createStudyLevel(r.code(), r.name(), r.yearOfStudy(), r.rhythm(),
                r.programId(), r.cycleId(), r.specializationId()));
    }

    @GetMapping("/school-classes")
    @PreAuthorize("@perm.can('SchoolClass', 'READ')")
    List<SchoolClassDto> schoolClasses(@RequestParam UUID academicYearId, @RequestParam(required = false) UUID studyLevelId) {
        return structureService.schoolClasses(academicYearId, studyLevelId).stream().map(SchoolClassDto::from).toList();
    }

    @PostMapping("/school-classes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('SchoolClass', 'CREATE')")
    SchoolClassDto createSchoolClass(@Valid @RequestBody SchoolClassRequest r) {
        if (r.studyLevelId() == null || r.academicYearId() == null) {
            throw new BusinessRuleException("schoolClass.required", "Study level and academic year are required.");
        }
        return SchoolClassDto.from(structureService.createSchoolClass(r.code(), r.name(), r.capacity(),
                r.studyLevelId(), r.academicYearId(), r.siteId()));
    }

    @PutMapping("/school-classes/{id}")
    @PreAuthorize("@perm.can('SchoolClass', 'WRITE')")
    SchoolClassDto updateSchoolClass(@PathVariable UUID id, @Valid @RequestBody SchoolClassRequest r) {
        return SchoolClassDto.from(structureService.updateSchoolClass(id, r.code(), r.name(), r.capacity(), r.siteId()));
    }

    /** Which portal sections are switched off (e.g. the student Paiements section). Any signed-in user. */
    @GetMapping("/feature-toggles")
    List<FeatureToggleDto> featureToggles(@RequestParam Portal portal) {
        return featureToggles.findAllByPortal(portal).stream().map(FeatureToggleDto::from).toList();
    }

    @GetMapping("/rooms")
    @PreAuthorize("@perm.can('Room', 'READ')")
    List<RoomInfo> rooms() {
        return directory.rooms();
    }
}
