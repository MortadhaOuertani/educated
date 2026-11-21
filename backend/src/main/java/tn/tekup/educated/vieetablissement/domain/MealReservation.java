package tn.tekup.educated.vieetablissement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import tn.tekup.educated.shared.domain.PersonType;
import tn.tekup.educated.shared.domain.TenantEntity;

@Entity
@Table(name = "meal_reservation", schema = "vieetablissement")
public class MealReservation extends TenantEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PersonType personType;

    @Column(nullable = false)
    private UUID personId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ReservationStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false)
    private CanteenMenu menu;

    public MealReservation() {
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

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public CanteenMenu getMenu() {
        return menu;
    }

    public void setMenu(CanteenMenu menu) {
        this.menu = menu;
    }

}
