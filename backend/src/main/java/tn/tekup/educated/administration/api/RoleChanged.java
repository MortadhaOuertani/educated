package tn.tekup.educated.administration.api;

import java.util.UUID;

/** Published when a role's permissions or holders change. */
public record RoleChanged(UUID establishmentId, UUID roleId) {
}
