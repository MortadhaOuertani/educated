package tn.tekup.educated.finances.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import tn.tekup.educated.shared.domain.Money;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Échéance. */
@Entity
@Table(name = "installment", schema = "finances")
public class Installment extends TenantEntity {

    @Column(nullable = false)
    private Integer installmentNumber;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Embedded
    private Money amount;

    @Embedded
    private Money paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private InstallmentStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_plan_id", nullable = false)
    private StudentFeePlan feePlan;

    @OneToMany(mappedBy = "installment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentReminder> reminders = new ArrayList<>();

    public Installment() {
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(Integer installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public Money getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Money paidAmount) {
        this.paidAmount = paidAmount;
    }

    public InstallmentStatus getStatus() {
        return status;
    }

    public void setStatus(InstallmentStatus status) {
        this.status = status;
    }

    public StudentFeePlan getFeePlan() {
        return feePlan;
    }

    public void setFeePlan(StudentFeePlan feePlan) {
        this.feePlan = feePlan;
    }

    public List<PaymentReminder> getReminders() {
        return reminders;
    }

}
