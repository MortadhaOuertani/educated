package tn.tekup.educated.parametrage.api;

import java.util.UUID;

/** Published when an academic year becomes the current one. */
public record AcademicYearOpened(UUID establishmentId, UUID academicYearId, String code) {
}
