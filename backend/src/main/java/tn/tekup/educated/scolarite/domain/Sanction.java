package tn.tekup.educated.scolarite.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A disciplinary sanction, including exclusions. */
@Entity
@Table(name = "sanction", schema = "scolarite")
public class Sanction extends TenantEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SanctionType sanctionType;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = false)
    private LocalDate incidentDate;

    private LocalDate startDate;

    private LocalDate endDate;

    private UUID issuedByUserId;

    private boolean notifyGuardians;

    private Instant notifiedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    public Sanction() {
    }

    public SanctionType getSanctionType() {
        return sanctionType;
    }

    public void setSanctionType(SanctionType sanctionType) {
        this.sanctionType = sanctionType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDate incidentDate) {
        this.incidentDate = incidentDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public UUID getIssuedByUserId() {
        return issuedByUserId;
    }

    public void setIssuedByUserId(UUID issuedByUserId) {
        this.issuedByUserId = issuedByUserId;
    }

    public boolean isNotifyGuardians() {
        return notifyGuardians;
    }

    public void setNotifyGuardians(boolean notifyGuardians) {
        this.notifyGuardians = notifyGuardians;
    }

    public Instant getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(Instant notifiedAt) {
        this.notifiedAt = notifiedAt;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

}
