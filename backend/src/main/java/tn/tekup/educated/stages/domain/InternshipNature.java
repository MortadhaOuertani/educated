package tn.tekup.educated.stages.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import tn.tekup.educated.shared.domain.LocalizedText;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Kind of internship (1st year, 2nd year, PFE). */
@Entity
@Table(name = "internship_nature", schema = "stages")
public class InternshipNature extends TenantEntity {

    @Column(nullable = false, length = 40)
    private String code;

    @Embedded
    private LocalizedText label;

    private UUID studyLevelId;

    private boolean requiresDefense;

    private Integer minWeeks;

    public InternshipNature() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalizedText getLabel() {
        return label;
    }

    public void setLabel(LocalizedText label) {
        this.label = label;
    }

    public UUID getStudyLevelId() {
        return studyLevelId;
    }

    public void setStudyLevelId(UUID studyLevelId) {
        this.studyLevelId = studyLevelId;
    }

    public boolean isRequiresDefense() {
        return requiresDefense;
    }

    public void setRequiresDefense(boolean requiresDefense) {
        this.requiresDefense = requiresDefense;
    }

    public Integer getMinWeeks() {
        return minWeeks;
    }

    public void setMinWeeks(Integer minWeeks) {
        this.minWeeks = minWeeks;
    }

}
