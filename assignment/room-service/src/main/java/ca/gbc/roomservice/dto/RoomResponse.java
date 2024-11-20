package ca.gbc.roomservice.dto;

public record RoomResponse(  String id,
                             String roomName,
                             int capacity,
                             String features,
                             boolean availability) {
}
