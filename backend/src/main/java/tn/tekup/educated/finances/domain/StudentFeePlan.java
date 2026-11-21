package tn.tekup.educated.finances.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import tn.tekup.educated.shared.domain.Money;
import tn.tekup.educated.shared.domain.TenantEntity;

/** What one enrolled student has to pay for the year. */
@Entity
@Table(name = "student_fee_plan", schema = "finances")
public class StudentFeePlan extends TenantEntity {

    @Column(nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private UUID enrollmentId;

    @Embedded
    private Money totalAmount;

    @Embedded
    private Money discount;

    @Embedded
    private Money scholarship;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FeePlanStatus status;

    @ManyToMany
    @JoinTable(name = "student_fee_plan_schedule", schema = "finances",
            joinColumns = @JoinColumn(name = "student_fee_plan_id"),
            inverseJoinColumns = @JoinColumn(name = "fee_schedule_id"))
    private Set<FeeSchedule> basedOn = new HashSet<>();

    @OneToMany(mappedBy = "feePlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("installmentNumber")
    private List<Installment> installments = new ArrayList<>();

    public StudentFeePlan() {
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(UUID enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Money totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Money getDiscount() {
        return discount;
    }

    public void setDiscount(Money discount) {
        this.discount = discount;
    }

    public Money getScholarship() {
        return scholarship;
    }

    public void setScholarship(Money scholarship) {
        this.scholarship = scholarship;
    }

    public FeePlanStatus getStatus() {
        return status;
    }

    public void setStatus(FeePlanStatus status) {
        this.status = status;
    }

    public Set<FeeSchedule> getBasedOn() {
        return basedOn;
    }

    public List<Installment> getInstallments() {
        return installments;
    }

}
