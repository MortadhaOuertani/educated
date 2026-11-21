package tn.tekup.educated.shared.security;

import java.util.List;
import java.util.UUID;

/** Read-only view of the establishments (tenants). Implemented by the parametrage module. */
public interface EstablishmentDirectory {

    List<UUID> allEstablishmentIds();

    boolean exists(UUID establishmentId);
}
