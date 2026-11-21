package tn.tekup.educated.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

/**
 * Base class for all data that belongs to one establishment. Hibernate fills {@code establishmentId}
 * from the current tenant on insert and adds it to every query, so one establishment can never read
 * another's rows. Business data is archived (soft-deleted) rather than removed.
 */
@MappedSuperclass
public abstract class TenantEntity extends BaseEntity {

    @TenantId
    @Column(nullable = false, updatable = false)
    private UUID establishmentId;

    private Instant archivedAt;

    public UUID getEstablishmentId() {
        return establishmentId;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }

    public boolean isArchived() {
        return archivedAt != null;
    }

    public void archive() {
        if (archivedAt == null) {
            archivedAt = Instant.now();
        }
    }

    public void restore() {
        archivedAt = null;
    }
}
