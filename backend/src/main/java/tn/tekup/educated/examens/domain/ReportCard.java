package tn.tekup.educated.examens.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Bulletin of a student for a period. */
@Entity
@Table(name = "report_card", schema = "examens")
public class ReportCard extends TenantEntity {

    @Column(nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private UUID academicPeriodId;

    @Column(precision = 5, scale = 2)
    private BigDecimal average;

    private Integer rankInClass;

    private Integer creditsEarned;

    private String appreciationText;

    private UUID pdfFileId;

    private Instant generatedAt;

    private boolean published;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appreciation_scale_id")
    private AppreciationScale appreciationScale;

    public ReportCard() {
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
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

    public Integer getRankInClass() {
        return rankInClass;
    }

    public void setRankInClass(Integer rankInClass) {
        this.rankInClass = rankInClass;
    }

    public Integer getCreditsEarned() {
        return creditsEarned;
    }

    public void setCreditsEarned(Integer creditsEarned) {
        this.creditsEarned = creditsEarned;
    }

    public String getAppreciationText() {
        return appreciationText;
    }

    public void setAppreciationText(String appreciationText) {
        this.appreciationText = appreciationText;
    }

    public UUID getPdfFileId() {
        return pdfFileId;
    }

    public void setPdfFileId(UUID pdfFileId) {
        this.pdfFileId = pdfFileId;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public AppreciationScale getAppreciationScale() {
        return appreciationScale;
    }

    public void setAppreciationScale(AppreciationScale appreciationScale) {
        this.appreciationScale = appreciationScale;
    }

}
