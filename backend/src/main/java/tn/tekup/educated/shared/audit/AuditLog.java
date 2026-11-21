package tn.tekup.educated.shared.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Append-only record of a sensitive action (grade change, payment, role change, publication...). */
@Entity
@Table(name = "audit_log", schema = "shared")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID establishmentId;

    @Column(nullable = false, length = 40)
    private String moduleName;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(nullable = false, length = 80)
    private String entityType;

    private UUID entityId;

    @Column(columnDefinition = "text")
    private String changesJson;

    private UUID actorUserId;

    @Column(length = 64)
    private String ipAddress;

    @Column(length = 512)
    private String userAgent;

    @Column(nullable = false)
    private Instant occurredAt;

    protected AuditLog() {
    }

    AuditLog(UUID establishmentId, String moduleName, String action, String entityType, UUID entityId,
            String changesJson, UUID actorUserId, String ipAddress, String userAgent) {
        this.establishmentId = establishmentId;
        this.moduleName = moduleName;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.changesJson = changesJson;
        this.actorUserId = actorUserId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.occurredAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getEstablishmentId() {
        return establishmentId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public String getChangesJson() {
        return changesJson;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
