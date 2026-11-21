package tn.tekup.educated.scolarite.api;

import java.util.UUID;

public record SanctionIssued(UUID establishmentId, UUID sanctionId, UUID studentId, boolean notifyGuardians) {
}
