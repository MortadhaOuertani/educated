package tn.tekup.educated.assiduite.api;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** A call was confirmed. Lists the students marked absent or late, so guardians and students can be told. */
public record AbsencesRecorded(UUID establishmentId, UUID sessionId, UUID schoolClassId, UUID teachingAssignmentId,
        LocalDate date, List<UUID> absentStudentIds, List<UUID> lateStudentIds) {
}
