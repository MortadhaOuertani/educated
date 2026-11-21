package tn.tekup.educated.finances.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import tn.tekup.educated.shared.domain.Money;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Tarification: a fee of a study level for an academic year. */
@Entity
@Table(name = "fee_schedule", schema = "finances")
public class FeeSchedule extends TenantEntity {

    @Column(nullable = false)
    private UUID academicYearId;

    @Column(nullable = false)
    private UUID studyLevelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FeeType feeType;

    @Embedded
    private Money amount;

    @Column(nullable = false)
    private Integer installmentsCount;

    private boolean active;

    public FeeSchedule() {
    }

    public UUID getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(UUID academicYearId) {
        this.academicYearId = academicYearId;
    }

    public UUID getStudyLevelId() {
        return studyLevelId;
    }

    public void setStudyLevelId(UUID studyLevelId) {
        this.studyLevelId = studyLevelId;
    }

    public FeeType getFeeType() {
        return feeType;
    }

    public void setFeeType(FeeType feeType) {
        this.feeType = feeType;
    }

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public Integer getInstallmentsCount() {
        return installmentsCount;
    }

    public void setInstallmentsCount(Integer installmentsCount) {
        this.installmentsCount = installmentsCount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
