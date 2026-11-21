package tn.tekup.educated.finances.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import tn.tekup.educated.shared.domain.Money;
import tn.tekup.educated.shared.domain.TenantEntity;

@Entity
@Table(name = "teacher_payment_line", schema = "finances")
public class TeacherPaymentLine extends TenantEntity {

    @Column(nullable = false)
    private UUID teacherAttendanceId;

    @Column(nullable = false)
    private LocalDate sessionDate;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal hours;

    @Embedded
    private Money amount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private TeacherPayment payment;

    public TeacherPaymentLine() {
    }

    public UUID getTeacherAttendanceId() {
        return teacherAttendanceId;
    }

    public void setTeacherAttendanceId(UUID teacherAttendanceId) {
        this.teacherAttendanceId = teacherAttendanceId;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(BigDecimal hours) {
        this.hours = hours;
    }

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public TeacherPayment getPayment() {
        return payment;
    }

    public void setPayment(TeacherPayment payment) {
        this.payment = payment;
    }

}
