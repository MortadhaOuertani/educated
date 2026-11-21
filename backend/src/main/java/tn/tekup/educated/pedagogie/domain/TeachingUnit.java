package tn.tekup.educated.pedagogie.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import tn.tekup.educated.shared.domain.LocalizedText;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A teaching unit (UE) of a study plan. */
@Entity
@Table(name = "teaching_unit", schema = "pedagogie")
public class TeachingUnit extends TenantEntity {

    @Column(nullable = false, length = 40)
    private String code;

    @Embedded
    private LocalizedText name;

    @Column(precision = 6, scale = 2)
    private BigDecimal coefficient;

    private Integer credits;

    @Column(nullable = false)
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_plan_id", nullable = false)
    private StudyPlan studyPlan;

    @OneToMany(mappedBy = "teachingUnit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyPlanSubject> subjects = new ArrayList<>();

    public TeachingUnit() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalizedText getName() {
        return name;
    }

    public void setName(LocalizedText name) {
        this.name = name;
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

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public StudyPlan getStudyPlan() {
        return studyPlan;
    }

    public void setStudyPlan(StudyPlan studyPlan) {
        this.studyPlan = studyPlan;
    }

    public List<StudyPlanSubject> getSubjects() {
        return subjects;
    }

}
