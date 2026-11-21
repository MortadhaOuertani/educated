package tn.tekup.educated.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Amount with currency. The Tunisian dinar has 3 decimals (millimes), hence scale 3. */
@Embeddable
public record Money(
        @Column(precision = 14, scale = 3) BigDecimal amount,
        @Column(length = 3) String currency) {

    public static final String TND = "TND";

    public static Money tnd(BigDecimal amount) {
        return new Money(amount.setScale(3, RoundingMode.HALF_UP), TND);
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch: " + currency + " vs " + other.currency);
        }
    }
}
