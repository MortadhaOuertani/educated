package tn.tekup.educated.scolarite.api;

import java.util.UUID;

public record StudentEnrolled(UUID establishmentId, UUID studentId, UUID enrollmentId, UUID academicYearId,
        UUID schoolClassId) {
}
