package tn.tekup.educated.pedagogie.api;

import java.util.UUID;
import tn.tekup.educated.shared.domain.SessionType;

/** One subject taught to one class, with the teacher if one is assigned. */
public record TeachingAssignmentInfo(UUID id, UUID teacherId, String teacherName, UUID subjectId, String subjectCode,
        String subjectName, UUID studyPlanSubjectId, UUID schoolClassId, UUID academicYearId,
        SessionType sessionType, Integer plannedHours) {
}
