package tn.tekup.educated.parametrage.api;

import java.util.UUID;

public record SchoolClassInfo(UUID id, String code, String name, UUID studyLevelId, UUID academicYearId) {
}
