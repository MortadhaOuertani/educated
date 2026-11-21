package tn.tekup.educated.shared.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Identity and contact details embedded in Student, Guardian, Teacher and StaffMember.
 * An embeddable (not a shared Person table) keeps each module's schema independent.
 */
@Embeddable
public record PersonInfo(
        String firstName,
        String lastName,
        String firstNameAr,
        String lastNameAr,
        @Enumerated(EnumType.STRING) Gender gender,
        LocalDate birthDate,
        String birthPlace,
        String nationality,
        String nationalIdNumber,
        String passportNumber,
        String email,
        String phone,
        String mobile,
        @Embedded Address address,
        UUID photoFileId) {

    public String fullName() {
        return (lastName == null ? "" : lastName.toUpperCase()) + " " + (firstName == null ? "" : firstName);
    }
}
