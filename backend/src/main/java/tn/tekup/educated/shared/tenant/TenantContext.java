package tn.tekup.educated.shared.tenant;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Holds the establishment (tenant) of the current request or job.
 *
 * <ul>
 *   <li>{@link #ROOT}: system work that may see every establishment (startup jobs, provisioning).</li>
 *   <li>{@link #NONE}: no establishment could be resolved; queries return nothing and inserts fail.</li>
 * </ul>
 */
public final class TenantContext {

    public static final UUID ROOT = new UUID(0L, 0L);
    public static final UUID NONE = new UUID(0L, 1L);

    private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    /** The tenant Hibernate must use: the current establishment, or NONE when nothing is set. */
    public static UUID currentOrNone() {
        UUID value = CURRENT.get();
        return value == null ? NONE : value;
    }

    /** The current establishment, if a real one is set. */
    public static Optional<UUID> currentEstablishment() {
        UUID value = CURRENT.get();
        return value == null || ROOT.equals(value) || NONE.equals(value) ? Optional.empty() : Optional.of(value);
    }

    /** The current establishment, for stamping domain events. Fails when no real establishment is set. */
    public static UUID requireEstablishment() {
        return currentEstablishment().orElseThrow(() -> new IllegalStateException("No establishment is set."));
    }

    public static void set(UUID establishmentId) {
        CURRENT.set(establishmentId);
    }

    public static void clear() {
        CURRENT.remove();
    }

    /** Runs the action inside the given establishment, then restores the previous tenant. */
    public static <T> T callAs(UUID establishmentId, Supplier<T> action) {
        UUID previous = CURRENT.get();
        CURRENT.set(establishmentId);
        try {
            return action.get();
        } finally {
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        }
    }

    public static void runAs(UUID establishmentId, Runnable action) {
        callAs(establishmentId, () -> {
            action.run();
            return null;
        });
    }

    /** Runs system work that must see all establishments. Use sparingly. */
    public static <T> T callAsRoot(Supplier<T> action) {
        return callAs(ROOT, action);
    }
}
