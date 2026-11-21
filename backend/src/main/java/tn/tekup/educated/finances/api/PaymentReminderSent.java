package tn.tekup.educated.finances.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** A reminder about an unpaid installment; the communication module delivers it. */
public record PaymentReminderSent(UUID establishmentId, UUID studentId, UUID installmentId, LocalDate dueDate,
        BigDecimal amountDue, String channel, String message) {
}
