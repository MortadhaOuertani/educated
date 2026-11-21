package tn.tekup.educated.examens.api;

import java.util.List;
import java.util.UUID;

public record ReportCardsPublished(UUID establishmentId, UUID schoolClassId, UUID academicPeriodId,
        List<UUID> studentIds) {
}
