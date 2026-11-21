package tn.tekup.educated.shared.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record Address(
        String line1,
        String line2,
        String city,
        String postalCode,
        String governorate,
        String country) {
}
