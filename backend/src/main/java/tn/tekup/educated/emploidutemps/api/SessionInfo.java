package tn.tekup.educated.emploidutemps.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import tn.tekup.educated.shared.domain.SessionType;

/** A dated session. Status is SCHEDULED, DONE, CANCELLED or TEACHER_ABSENT. */
public record SessionInfo(UUID id, UUID teachingAssignmentId, UUID teacherId, UUID subjectId, UUID schoolClassId,
        UUID roomId, LocalDate date, LocalTime startTime, LocalTime endTime, SessionType sessionType, String status,
        boolean makeUp) {

    /** Length of the session in hours, e.g. 1.50 for 90 minutes. */
    public BigDecimal hours() {
        long minutes = Duration.between(startTime, endTime).toMinutes();
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
}
