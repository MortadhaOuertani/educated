package tn.tekup.educated.examens.api;

import java.util.List;
import java.util.UUID;

/** The exam calendar of a session was published: students and invigilators can see it. */
public record ExamCalendarPublished(UUID establishmentId, UUID examSessionId, String name, List<UUID> schoolClassIds,
        List<UUID> invigilatorTeacherIds) {
}
