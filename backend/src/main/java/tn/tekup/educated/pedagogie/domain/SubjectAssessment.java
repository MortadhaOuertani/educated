package tn.tekup.educated.pedagogie.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import tn.tekup.educated.shared.domain.AssessmentType;
import tn.tekup.educated.shared.domain.TenantEntity;

/** One grade component of a subject and its weight. */
@Entity
@Table(name = "subject_assessment", schema = "pedagogie")
public class SubjectAssessment extends TenantEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AssessmentType assessmentType;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weightPercent;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal maxScore;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_plan_subject_id", nullable = false)
    private StudyPlanSubject studyPlanSubject;

    public SubjectAssessment() {
    }

    public AssessmentType getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(AssessmentType assessmentType) {
        this.assessmentType = assessmentType;
    }

    public BigDecimal getWeightPercent() {
        return weightPercent;
    }

    public void setWeightPercent(BigDecimal weightPercent) {
        this.weightPercent = weightPercent;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
    }

    public StudyPlanSubject getStudyPlanSubject() {
        return studyPlanSubject;
    }

    public void setStudyPlanSubject(StudyPlanSubject studyPlanSubject) {
        this.studyPlanSubject = studyPlanSubject;
    }

}
