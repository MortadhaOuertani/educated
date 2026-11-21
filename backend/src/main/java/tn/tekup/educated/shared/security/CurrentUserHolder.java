package tn.tekup.educated.shared.security;

import java.util.Optional;

/**
 * The user resolved for the current request, set once by the tenant filter.
 * Auditing reads it from here so that saving an entity never triggers a second user lookup.
 */
public final class CurrentUserHolder {

    private static final ThreadLocal<CurrentUser> CURRENT = new ThreadLocal<>();

    private CurrentUserHolder() {
    }

    public static Optional<CurrentUser> get() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static void set(CurrentUser user) {
        CURRENT.set(user);
    }

    public static void clear() {
        CURRENT.remove();
    }
}
