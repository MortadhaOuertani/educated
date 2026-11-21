package tn.tekup.educated.stages.domain;

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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A defense (soutenance) and its grade. */
@Entity
@Table(name = "defense", schema = "stages")
public class Defense extends TenantEntity {

    @Column(nullable = false)
    private LocalDate defenseDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DefenseStatus status;

    @Column(precision = 5, scale = 2)
    private BigDecimal grade;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private Mention mention;

    private boolean gradeValidated;

    private boolean published;

    private Instant publishedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jury_id", nullable = false)
    private Jury jury;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "internship_id", nullable = false)
    private Internship internship;

    @OneToMany(mappedBy = "defense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DefenseEvaluation> evaluations = new ArrayList<>();

    public Defense() {
    }

    public LocalDate getDefenseDate() {
        return defenseDate;
    }

    public void setDefenseDate(LocalDate defenseDate) {
        this.defenseDate = defenseDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public DefenseStatus getStatus() {
        return status;
    }

    public void setStatus(DefenseStatus status) {
        this.status = status;
    }

    public BigDecimal getGrade() {
        return grade;
    }

    public void setGrade(BigDecimal grade) {
        this.grade = grade;
    }

    public Mention getMention() {
        return mention;
    }

    public void setMention(Mention mention) {
        this.mention = mention;
    }

    public boolean isGradeValidated() {
        return gradeValidated;
    }

    public void setGradeValidated(boolean gradeValidated) {
        this.gradeValidated = gradeValidated;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Jury getJury() {
        return jury;
    }

    public void setJury(Jury jury) {
        this.jury = jury;
    }

    public Internship getInternship() {
        return internship;
    }

    public void setInternship(Internship internship) {
        this.internship = internship;
    }

    public List<DefenseEvaluation> getEvaluations() {
        return evaluations;
    }

}
