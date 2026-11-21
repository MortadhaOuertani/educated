package tn.tekup.educated.stages.api;

import java.math.BigDecimal;
import java.util.UUID;

/** Published grade of an internship defense. */
public record DefenseGrade(UUID internshipId, UUID studentId, UUID academicYearId, String natureCode,
        BigDecimal grade, String mention) {
}
