package tn.tekup.educated.pedagogie.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import tn.tekup.educated.shared.domain.LocalizedText;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A subject (matière). */
@Entity
@Table(name = "subject", schema = "pedagogie")
public class Subject extends TenantEntity {

    @Column(nullable = false, length = 40)
    private String code;

    @Embedded
    private LocalizedText name;

    @Column(length = 1000)
    private String description;

    private UUID teachingDomainId;

    @Column(precision = 6, scale = 2)
    private BigDecimal defaultCoefficient;

    private Integer defaultCredits;

    private boolean active;

    public Subject() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getTeachingDomainId() {
        return teachingDomainId;
    }

    public void setTeachingDomainId(UUID teachingDomainId) {
        this.teachingDomainId = teachingDomainId;
    }

    public BigDecimal getDefaultCoefficient() {
        return defaultCoefficient;
    }

    public void setDefaultCoefficient(BigDecimal defaultCoefficient) {
        this.defaultCoefficient = defaultCoefficient;
    }

    public Integer getDefaultCredits() {
        return defaultCredits;
    }

    public void setDefaultCredits(Integer defaultCredits) {
        this.defaultCredits = defaultCredits;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
