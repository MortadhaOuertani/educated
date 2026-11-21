package tn.tekup.educated.vieetablissement.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealReservationRepository extends JpaRepository<MealReservation, UUID> {

    Optional<MealReservation> findByMenu_IdAndPersonId(UUID menuId, UUID personId);

    List<MealReservation> findAllByPersonIdOrderByMenu_MenuDateDesc(UUID personId);

    long countByMenu_IdAndStatus(UUID menuId, ReservationStatus status);
}
