package tn.tekup.educated.assiduite.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Hours a teacher taught in one session, once validated by the administration. */
public record TeacherHoursInfo(UUID id, UUID teacherId, UUID sessionId, LocalDate date, BigDecimal hours) {
}
