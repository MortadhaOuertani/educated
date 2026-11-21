package tn.tekup.educated.pedagogie.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A subject inside a teaching unit, with coefficient, credits and hours. */
@Entity
@Table(name = "study_plan_subject", schema = "pedagogie")
public class StudyPlanSubject extends TenantEntity {

    @Column(precision = 6, scale = 2)
    private BigDecimal coefficient;

    private Integer credits;

    private Integer lectureHours;

    private Integer tutorialHours;

    private Integer labHours;

    private Integer projectHours;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teaching_unit_id", nullable = false)
    private TeachingUnit teachingUnit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @OneToMany(mappedBy = "studyPlanSubject", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubjectAssessment> assessments = new ArrayList<>();

    public StudyPlanSubject() {
    }

    public BigDecimal getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(BigDecimal coefficient) {
        this.coefficient = coefficient;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public Integer getLectureHours() {
        return lectureHours;
    }

    public void setLectureHours(Integer lectureHours) {
        this.lectureHours = lectureHours;
    }

    public Integer getTutorialHours() {
        return tutorialHours;
    }

    public void setTutorialHours(Integer tutorialHours) {
        this.tutorialHours = tutorialHours;
    }

    public Integer getLabHours() {
        return labHours;
    }

    public void setLabHours(Integer labHours) {
        this.labHours = labHours;
    }

    public Integer getProjectHours() {
        return projectHours;
    }

    public void setProjectHours(Integer projectHours) {
        this.projectHours = projectHours;
    }

    public TeachingUnit getTeachingUnit() {
        return teachingUnit;
    }

    public void setTeachingUnit(TeachingUnit teachingUnit) {
        this.teachingUnit = teachingUnit;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public List<SubjectAssessment> getAssessments() {
        return assessments;
    }

}
