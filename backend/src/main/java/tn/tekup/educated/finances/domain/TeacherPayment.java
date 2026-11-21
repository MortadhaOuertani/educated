package tn.tekup.educated.finances.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tn.tekup.educated.shared.domain.Money;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Payment of a teacher's validated hours (Payer / Annuler paiements). */
@Entity
@Table(name = "teacher_payment", schema = "finances")
public class TeacherPayment extends TenantEntity {

    @Column(nullable = false)
    private UUID teacherId;

    @Column(nullable = false)
    private LocalDate periodFrom;

    @Column(nullable = false)
    private LocalDate periodTo;

    @Column(precision = 7, scale = 2)
    private BigDecimal totalHours;

    @Embedded
    private Money hourlyRate;

    @Embedded
    private Money totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TeacherPaymentStatus status;

    private UUID paidByUserId;

    private Instant paidAt;

    private Instant cancelledAt;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherPaymentLine> lines = new ArrayList<>();

    public TeacherPayment() {
    }

    public UUID getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(UUID teacherId) {
        this.teacherId = teacherId;
    }

    public LocalDate getPeriodFrom() {
        return periodFrom;
    }

    public void setPeriodFrom(LocalDate periodFrom) {
        this.periodFrom = periodFrom;
    }

    public LocalDate getPeriodTo() {
        return periodTo;
    }

    public void setPeriodTo(LocalDate periodTo) {
        this.periodTo = periodTo;
    }

    public BigDecimal getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(BigDecimal totalHours) {
        this.totalHours = totalHours;
    }

    public Money getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Money hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Money totalAmount) {
        this.totalAmount = totalAmount;
    }

    public TeacherPaymentStatus getStatus() {
        return status;
    }

    public void setStatus(TeacherPaymentStatus status) {
        this.status = status;
    }

    public UUID getPaidByUserId() {
        return paidByUserId;
    }

    public void setPaidByUserId(UUID paidByUserId) {
        this.paidByUserId = paidByUserId;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public List<TeacherPaymentLine> getLines() {
        return lines;
    }

}
