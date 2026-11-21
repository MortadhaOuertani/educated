package tn.tekup.educated.scolarite.api;

import java.util.UUID;

public record DocumentRequestStatusChanged(UUID establishmentId, UUID requestId, UUID studentId, String status) {
}
