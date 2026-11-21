package tn.tekup.educated.emploidutemps.api;

import java.time.LocalDate;
import java.util.UUID;

public record SessionCancelled(UUID establishmentId, UUID sessionId, UUID schoolClassId, UUID teachingAssignmentId,
        LocalDate date, String reason) {
}
