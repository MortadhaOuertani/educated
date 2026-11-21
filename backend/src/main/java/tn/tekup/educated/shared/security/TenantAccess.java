package tn.tekup.educated.shared.security;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Which establishments a user may work in. Implemented by the administration module. */
public interface TenantAccess {

    /** Establishments where the user holds at least one role (all establishments for administrative users). */
    Set<UUID> accessibleEstablishments(CurrentUser user);

    /** The establishment to use when the client does not choose one. */
    Optional<UUID> defaultEstablishment(CurrentUser user);

    default boolean canAccess(CurrentUser user, UUID establishmentId) {
        return accessibleEstablishments(user).contains(establishmentId);
    }
}
