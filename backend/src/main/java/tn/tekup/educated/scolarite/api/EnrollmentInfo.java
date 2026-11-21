package tn.tekup.educated.scolarite.api;

import java.util.UUID;

/** The student's current enrollment: year, study level and class. */
public record EnrollmentInfo(UUID enrollmentId, UUID studentId, UUID academicYearId, UUID studyLevelId,
        UUID schoolClassId) {
}
