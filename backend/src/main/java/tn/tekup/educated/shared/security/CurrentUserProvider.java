package tn.tekup.educated.shared.security;

import java.util.Optional;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import tn.tekup.educated.shared.domain.PersonType;

/** Resolves the application user behind the current request. Implemented by the administration module. */
public interface CurrentUserProvider {

    Optional<CurrentUser> current();

    default CurrentUser require() {
        return current().orElseThrow(() -> new AccessDeniedException("Not signed in"));
    }

    /**
     * The id of the student, teacher or guardian the signed-in account belongs to. Portal endpoints use it so a
     * user only ever reaches their own data.
     */
    default UUID requirePerson(PersonType type) {
        CurrentUser user = require();
        if (!user.is(type)) {
            throw new AccessDeniedException("This page is for " + type.name().toLowerCase() + " accounts.");
        }
        return user.personId();
    }
}
