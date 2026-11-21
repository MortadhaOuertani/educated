package tn.tekup.educated.pedagogie.api;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import tn.tekup.educated.shared.domain.AssessmentType;

/** Coefficient, credits and grade weights (in %) of a subject in a study plan. */
public record StudyPlanSubjectInfo(UUID id, UUID subjectId, String subjectCode, String subjectName,
        BigDecimal coefficient, Integer credits, Map<AssessmentType, BigDecimal> weights) {
}
