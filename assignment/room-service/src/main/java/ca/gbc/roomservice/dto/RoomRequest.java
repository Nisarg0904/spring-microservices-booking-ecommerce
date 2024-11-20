package ca.gbc.roomservice.dto;

public record RoomRequest(
         String id,
         String roomName,
         int capacity,
         String features,
         boolean availability
) {
}
