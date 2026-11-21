package tn.tekup.educated.emploidutemps.api;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/** A single session was added outside the weekly timetable (a make-up session). */
public record SessionScheduled(UUID establishmentId, UUID sessionId, UUID schoolClassId, UUID teachingAssignmentId,
        LocalDate date, LocalTime startTime, boolean makeUp) {
}
