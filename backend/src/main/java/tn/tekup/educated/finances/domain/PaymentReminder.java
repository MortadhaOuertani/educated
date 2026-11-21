package tn.tekup.educated.finances.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Relance for an unpaid installment. */
@Entity
@Table(name = "payment_reminder", schema = "finances")
public class PaymentReminder extends TenantEntity {

    @Column(nullable = false)
    private Integer reminderLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Channel channel;

    @Column(length = 1000)
    private String message;

    private Instant sentAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "installment_id", nullable = false)
    private Installment installment;

    public PaymentReminder() {
    }

    public Integer getReminderLevel() {
        return reminderLevel;
    }

    public void setReminderLevel(Integer reminderLevel) {
        this.reminderLevel = reminderLevel;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public Installment getInstallment() {
        return installment;
    }

    public void setInstallment(Installment installment) {
        this.installment = installment;
    }

}
