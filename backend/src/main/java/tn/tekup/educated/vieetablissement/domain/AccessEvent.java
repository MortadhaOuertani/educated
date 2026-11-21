package tn.tekup.educated.vieetablissement.domain;

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
import tn.tekup.educated.shared.domain.PersonType;
import tn.tekup.educated.shared.domain.TenantEntity;

@Entity
@Table(name = "access_event", schema = "vieetablissement")
public class AccessEvent extends TenantEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PersonType personType;

    @Column(nullable = false)
    private UUID personId;

    @Column(nullable = false)
    private Instant occurredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AccessDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AccessMethod method;

    private boolean granted;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "access_point_id", nullable = false)
    private AccessPoint accessPoint;

    public AccessEvent() {
    }

    public PersonType getPersonType() {
        return personType;
    }

    public void setPersonType(PersonType personType) {
        this.personType = personType;
    }

    public UUID getPersonId() {
        return personId;
    }

    public void setPersonId(UUID personId) {
        this.personId = personId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public AccessDirection getDirection() {
        return direction;
    }

    public void setDirection(AccessDirection direction) {
        this.direction = direction;
    }

    public AccessMethod getMethod() {
        return method;
    }

    public void setMethod(AccessMethod method) {
        this.method = method;
    }

    public boolean isGranted() {
        return granted;
    }

    public void setGranted(boolean granted) {
        this.granted = granted;
    }

    public AccessPoint getAccessPoint() {
        return accessPoint;
    }

    public void setAccessPoint(AccessPoint accessPoint) {
        this.accessPoint = accessPoint;
    }

}
