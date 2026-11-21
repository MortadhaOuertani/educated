package tn.tekup.educated.scolarite.api;

import java.util.UUID;

public record StudentInfo(UUID id, String matricule, String fullName) {
}
