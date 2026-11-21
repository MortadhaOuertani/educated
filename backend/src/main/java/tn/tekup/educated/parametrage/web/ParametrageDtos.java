package tn.tekup.educated.parametrage.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import tn.tekup.educated.parametrage.domain.AcademicPeriod;
import tn.tekup.educated.parametrage.domain.AcademicYear;
import tn.tekup.educated.parametrage.domain.AcademicYearStatus;
import tn.tekup.educated.parametrage.domain.Cycle;
import tn.tekup.educated.parametrage.domain.Establishment;
import tn.tekup.educated.parametrage.domain.FeatureToggle;
import tn.tekup.educated.parametrage.domain.Portal;
import tn.tekup.educated.parametrage.domain.Program;
import tn.tekup.educated.parametrage.domain.SchoolClass;
import tn.tekup.educated.parametrage.domain.StudyLevel;
import tn.tekup.educated.parametrage.domain.StudyRhythm;
import tn.tekup.educated.shared.domain.LocalizedText;

/** Request and response bodies of the parametrage REST API. */
final class ParametrageDtos {

    private ParametrageDtos() {
    }

    record EstablishmentDto(UUID id, String code, LocalizedText name) {
        static EstablishmentDto from(Establishment e) {
            return new EstablishmentDto(e.getId(), e.getCode(), e.getName());
        }
    }

    record PeriodDto(UUID id, String code, LocalizedText name, int orderIndex, LocalDate startDate, LocalDate endDate) {
        static PeriodDto from(AcademicPeriod p) {
            return new PeriodDto(p.getId(), p.getCode(), p.getName(), p.getOrderIndex(), p.getStartDate(), p.getEndDate());
        }
    }

    record AcademicYearDto(UUID id, String code, LocalDate startDate, LocalDate endDate, AcademicYearStatus status,
            boolean current, List<PeriodDto> periods) {
        static AcademicYearDto from(AcademicYear y) {
            return new AcademicYearDto(y.getId(), y.getCode(), y.getStartDate(), y.getEndDate(), y.getStatus(),
                    y.isCurrent(), y.getPeriods().stream().map(PeriodDto::from).toList());
        }
    }

    record NewPeriodRequest(@NotBlank String code, @NotBlank String name, @NotNull LocalDate startDate,
            @NotNull LocalDate endDate) {
    }

    record NewAcademicYearRequest(@NotBlank @Size(max = 20) String code, @NotNull LocalDate startDate,
            @NotNull LocalDate endDate, @Valid List<NewPeriodRequest> periods) {
    }

    record CycleDto(UUID id, String code, LocalizedText name, Integer durationYears) {
        static CycleDto from(Cycle c) {
            return new CycleDto(c.getId(), c.getCode(), c.getName(), c.getDurationYears());
        }
    }

    record NewCycleRequest(@NotBlank @Size(max = 20) String code, @NotBlank String name, Integer durationYears) {
    }

    record ProgramDto(UUID id, String code, LocalizedText name, String diplomaTitle, boolean active) {
        static ProgramDto from(Program p) {
            return new ProgramDto(p.getId(), p.getCode(), p.getName(), p.getDiplomaTitle(), p.isActive());
        }
    }

    record NewProgramRequest(@NotBlank @Size(max = 20) String code, @NotBlank String name, String diplomaTitle) {
    }

    record StudyLevelDto(UUID id, String code, LocalizedText name, int yearOfStudy, StudyRhythm rhythm,
            UUID programId, UUID cycleId, UUID specializationId) {
        static StudyLevelDto from(StudyLevel l) {
            return new StudyLevelDto(l.getId(), l.getCode(), l.getName(), l.getYearOfStudy(), l.getRhythm(),
                    l.getProgram().getId(), l.getCycle().getId(),
                    l.getSpecialization() == null ? null : l.getSpecialization().getId());
        }
    }

    record NewStudyLevelRequest(@NotBlank @Size(max = 40) String code, @NotBlank String name,
            @NotNull @Min(1) Integer yearOfStudy, @NotNull StudyRhythm rhythm, @NotNull UUID programId,
            @NotNull UUID cycleId, UUID specializationId) {
    }

    record SchoolClassDto(UUID id, String code, String name, Integer capacity, UUID studyLevelId, UUID academicYearId,
            UUID siteId) {
        static SchoolClassDto from(SchoolClass c) {
            return new SchoolClassDto(c.getId(), c.getCode(), c.getName(), c.getCapacity(), c.getStudyLevel().getId(),
                    c.getAcademicYear().getId(), c.getSite() == null ? null : c.getSite().getId());
        }
    }

    record SchoolClassRequest(@NotBlank @Size(max = 40) String code, String name, @Min(1) Integer capacity,
            UUID studyLevelId, UUID academicYearId, UUID siteId) {
    }

    record FeatureToggleDto(String featureKey, Portal portal, boolean enabled, String disabledMessage) {
        static FeatureToggleDto from(FeatureToggle t) {
            return new FeatureToggleDto(t.getFeatureKey(), t.getPortal(), t.isEnabled(), t.getDisabledMessage());
        }
    }
}
