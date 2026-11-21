package tn.tekup.educated.vieetablissement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import tn.tekup.educated.shared.domain.Money;
import tn.tekup.educated.shared.domain.TenantEntity;

@Entity
@Table(name = "canteen_menu", schema = "vieetablissement")
public class CanteenMenu extends TenantEntity {

    @Column(nullable = false)
    private LocalDate menuDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private MealType mealType;

    @Column(columnDefinition = "text")
    private String items;

    @Embedded
    private Money price;

    public CanteenMenu() {
    }

    public LocalDate getMenuDate() {
        return menuDate;
    }

    public void setMenuDate(LocalDate menuDate) {
        this.menuDate = menuDate;
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public Money getPrice() {
        return price;
    }

    public void setPrice(Money price) {
        this.price = price;
    }

}
