package tn.tekup.educated.pedagogie.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A study plan (plan d'étude), e.g. ING-1-INFO-Semestre 1. */
@Entity
@Table(name = "study_plan", schema = "pedagogie")
public class StudyPlan extends TenantEntity {

    @Column(nullable = false, length = 80)
    private String code;

    private String description;

    @Column(nullable = false)
    private UUID studyLevelId;

    @Column(nullable = false)
    private Integer semesterNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private StudyPlanStatus status;

    @OneToMany(mappedBy = "studyPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex")
    private List<TeachingUnit> units = new ArrayList<>();

    public StudyPlan() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getStudyLevelId() {
        return studyLevelId;
    }

    public void setStudyLevelId(UUID studyLevelId) {
        this.studyLevelId = studyLevelId;
    }

    public Integer getSemesterNumber() {
        return semesterNumber;
    }

    public void setSemesterNumber(Integer semesterNumber) {
        this.semesterNumber = semesterNumber;
    }

    public StudyPlanStatus getStatus() {
        return status;
    }

    public void setStatus(StudyPlanStatus status) {
        this.status = status;
    }

    public List<TeachingUnit> getUnits() {
        return units;
    }

}
