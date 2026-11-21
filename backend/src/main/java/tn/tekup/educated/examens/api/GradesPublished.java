package tn.tekup.educated.examens.api;

import java.util.UUID;

/** The grades of one assessment were published to the students of a class. */
public record GradesPublished(UUID establishmentId, UUID gradeGridId, UUID schoolClassId, UUID studyPlanSubjectId,
        String assessmentType) {
}
