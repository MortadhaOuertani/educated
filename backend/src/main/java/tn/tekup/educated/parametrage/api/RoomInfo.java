package tn.tekup.educated.parametrage.api;

import java.util.UUID;

public record RoomInfo(UUID id, String code, String name, Integer capacity, String roomType) {
}
