package tn.tekup.educated.parametrage.api;

import java.time.LocalDate;
import java.util.UUID;

/** A semester or trimester of an academic year. */
public record AcademicPeriodInfo(UUID id, String code, String name, int orderIndex, LocalDate startDate,
        LocalDate endDate, UUID academicYearId) {
}
