package tn.tekup.educated.parametrage.api;

import java.time.LocalDate;
import java.util.UUID;

public record AcademicYearInfo(UUID id, String code, LocalDate startDate, LocalDate endDate) {
}
