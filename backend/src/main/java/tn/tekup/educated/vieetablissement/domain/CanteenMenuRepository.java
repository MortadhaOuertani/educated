package tn.tekup.educated.vieetablissement.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CanteenMenuRepository extends JpaRepository<CanteenMenu, UUID> {

    List<CanteenMenu> findAllByMenuDateBetweenOrderByMenuDateAscMealTypeAsc(LocalDate from, LocalDate to);

    boolean existsByMenuDateAndMealType(LocalDate menuDate, MealType mealType);
}
