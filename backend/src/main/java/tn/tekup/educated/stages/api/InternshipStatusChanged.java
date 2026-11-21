package tn.tekup.educated.stages.api;

import java.util.UUID;

/** An internship moved to a new step (approved, refused, defense requested, authorized, validated...). */
public record InternshipStatusChanged(UUID establishmentId, UUID internshipId, UUID studentId,
        UUID academicSupervisorId, String status, String comment) {
}
