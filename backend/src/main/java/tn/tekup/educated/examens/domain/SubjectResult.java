package tn.tekup.educated.examens.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Average of a student in a subject for a period. */
@Entity
@Table(name = "subject_result", schema = "examens")
public class SubjectResult extends TenantEntity {

    @Column(nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private UUID studyPlanSubjectId;

    @Column(nullable = false)
    private UUID academicPeriodId;

    @Column(precision = 5, scale = 2)
    private BigDecimal average;

    private Integer creditsEarned;

    private boolean validated;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ResultSession resultSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_card_id")
    private ReportCard reportCard;

    public SubjectResult() {
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public UUID getStudyPlanSubjectId() {
        return studyPlanSubjectId;
    }

    public void setStudyPlanSubjectId(UUID studyPlanSubjectId) {
        this.studyPlanSubjectId = studyPlanSubjectId;
    }

    public UUID getAcademicPeriodId() {
        return academicPeriodId;
    }

    public void setAcademicPeriodId(UUID academicPeriodId) {
        this.academicPeriodId = academicPeriodId;
    }

    public BigDecimal getAverage() {
        return average;
    }

    public void setAverage(BigDecimal average) {
        this.average = average;
    }

    public Integer getCreditsEarned() {
        return creditsEarned;
    }

    public void setCreditsEarned(Integer creditsEarned) {
        this.creditsEarned = creditsEarned;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public ResultSession getResultSession() {
        return resultSession;
    }

    public void setResultSession(ResultSession resultSession) {
        this.resultSession = resultSession;
    }

    public ReportCard getReportCard() {
        return reportCard;
    }

    public void setReportCard(ReportCard reportCard) {
        this.reportCard = reportCard;
    }

}
