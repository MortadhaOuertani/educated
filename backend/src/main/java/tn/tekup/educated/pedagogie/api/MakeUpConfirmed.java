package tn.tekup.educated.pedagogie.api;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/** A make-up session was approved; the timetable module schedules it. */
public record MakeUpConfirmed(UUID establishmentId, UUID makeUpRequestId, UUID originalOccurrenceId,
        UUID teachingAssignmentId, LocalDate date, LocalTime startTime, LocalTime endTime, UUID roomId) {
}
