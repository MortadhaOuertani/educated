package tn.tekup.educated.emploidutemps.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Règle pédagogique used by the timetable generator. */
@Entity
@Table(name = "scheduling_rule", schema = "emploidutemps")
public class SchedulingRule extends TenantEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RuleType ruleType;

    @Column(columnDefinition = "text")
    private String parametersJson;

    private Integer weight;

    private boolean hardConstraint;

    private UUID studyLevelId;

    private UUID schoolClassId;

    private UUID teacherId;

    private boolean active;

    public SchedulingRule() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    public String getParametersJson() {
        return parametersJson;
    }

    public void setParametersJson(String parametersJson) {
        this.parametersJson = parametersJson;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public boolean isHardConstraint() {
        return hardConstraint;
    }

    public void setHardConstraint(boolean hardConstraint) {
        this.hardConstraint = hardConstraint;
    }

    public UUID getStudyLevelId() {
        return studyLevelId;
    }

    public void setStudyLevelId(UUID studyLevelId) {
        this.studyLevelId = studyLevelId;
    }

    public UUID getSchoolClassId() {
        return schoolClassId;
    }

    public void setSchoolClassId(UUID schoolClassId) {
        this.schoolClassId = schoolClassId;
    }

    public UUID getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(UUID teacherId) {
        this.teacherId = teacherId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
