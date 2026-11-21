package tn.tekup.educated.finances.api;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentReceived(UUID establishmentId, UUID paymentId, UUID studentId, BigDecimal amount,
        String receiptNumber) {
}
