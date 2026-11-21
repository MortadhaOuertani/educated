package tn.tekup.educated.pedagogie.api;

import java.util.UUID;
import tn.tekup.educated.shared.domain.Money;

public record TeacherInfo(UUID id, String employeeNumber, String fullName, Money hourlyRate, boolean active) {
}
