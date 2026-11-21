package tn.tekup.educated.examens.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tn.tekup.educated.shared.domain.AssessmentType;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Grille de notes: one assessment of one subject for one class. */
@Entity
@Table(name = "grade_grid", schema = "examens")
public class GradeGrid extends TenantEntity {

    @Column(nullable = false)
    private UUID studyPlanSubjectId;

    @Column(nullable = false)
    private UUID schoolClassId;

    @Column(nullable = false)
    private UUID academicPeriodId;

    private UUID teacherId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AssessmentType assessmentType;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal maxScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private GradeGridStatus status;

    private Instant confirmedAt;

    private Instant publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id")
    private Exam exam;

    @OneToMany(mappedBy = "grid", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GradeEntry> entries = new ArrayList<>();

    public GradeGrid() {
    }

    public UUID getStudyPlanSubjectId() {
        return studyPlanSubjectId;
    }

    public void setStudyPlanSubjectId(UUID studyPlanSubjectId) {
        this.studyPlanSubjectId = studyPlanSubjectId;
    }

    public UUID getSchoolClassId() {
        return schoolClassId;
    }

    public void setSchoolClassId(UUID schoolClassId) {
        this.schoolClassId = schoolClassId;
    }

    public UUID getAcademicPeriodId() {
        return academicPeriodId;
    }

    public void setAcademicPeriodId(UUID academicPeriodId) {
        this.academicPeriodId = academicPeriodId;
    }

    public UUID getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(UUID teacherId) {
        this.teacherId = teacherId;
    }

    public AssessmentType getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(AssessmentType assessmentType) {
        this.assessmentType = assessmentType;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
    }

    public GradeGridStatus getStatus() {
        return status;
    }

    public void setStatus(GradeGridStatus status) {
        this.status = status;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public List<GradeEntry> getEntries() {
        return entries;
    }

}
