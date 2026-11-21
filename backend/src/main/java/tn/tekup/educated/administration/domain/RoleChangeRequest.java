package tn.tekup.educated.administration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A sensitive role change waiting for a second Super Admin's approval. */
@Entity
@Table(name = "role_change_request", schema = "administration")
public class RoleChangeRequest extends TenantEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RoleChangeType changeType;

    @Column(nullable = false, columnDefinition = "text")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ApprovalStatus status;

    private UUID decidedByUserId;

    private Instant decidedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    public RoleChangeRequest() {
    }

    public RoleChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(RoleChangeType changeType) {
        this.changeType = changeType;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }

    public UUID getDecidedByUserId() {
        return decidedByUserId;
    }

    public void setDecidedByUserId(UUID decidedByUserId) {
        this.decidedByUserId = decidedByUserId;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(Instant decidedAt) {
        this.decidedAt = decidedAt;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

}
