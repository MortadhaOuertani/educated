package tn.tekup.educated.shared.security;

import java.util.UUID;
import tn.tekup.educated.shared.domain.PersonType;

/**
 * The signed-in user as the application sees it.
 *
 * @param userId        id of the application's UserAccount (stored in createdByUserId / updatedByUserId)
 * @param keycloakId    subject of the Keycloak token
 * @param personType    kind of person the account is linked to, or null if not linked yet
 * @param personId      id of the linked Student, Teacher, Guardian or StaffMember, or null
 * @param administrative true when one of the user's roles is administrative (bypasses permissions)
 */
public record CurrentUser(
        UUID userId,
        UUID keycloakId,
        String username,
        String email,
        String firstName,
        String lastName,
        PersonType personType,
        UUID personId,
        boolean administrative) {

    public boolean is(PersonType type) {
        return personType == type && personId != null;
    }
}
