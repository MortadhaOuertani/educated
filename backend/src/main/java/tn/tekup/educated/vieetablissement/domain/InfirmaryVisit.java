package tn.tekup.educated.vieetablissement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

@Entity
@Table(name = "infirmary_visit", schema = "vieetablissement")
public class InfirmaryVisit extends TenantEntity {

    @Column(nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private Instant visitedAt;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(columnDefinition = "text")
    private String careGiven;

    private boolean guardiansNotified;

    @Column(length = 1000)
    private String followUp;

    public InfirmaryVisit() {
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public Instant getVisitedAt() {
        return visitedAt;
    }

    public void setVisitedAt(Instant visitedAt) {
        this.visitedAt = visitedAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCareGiven() {
        return careGiven;
    }

    public void setCareGiven(String careGiven) {
        this.careGiven = careGiven;
    }

    public boolean isGuardiansNotified() {
        return guardiansNotified;
    }

    public void setGuardiansNotified(boolean guardiansNotified) {
        this.guardiansNotified = guardiansNotified;
    }

    public String getFollowUp() {
        return followUp;
    }

    public void setFollowUp(String followUp) {
        this.followUp = followUp;
    }

}
