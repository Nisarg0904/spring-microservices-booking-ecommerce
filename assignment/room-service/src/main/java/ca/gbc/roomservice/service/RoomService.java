package ca.gbc.roomservice.service;

import ca.gbc.roomservice.dto.RoomRequest;
import ca.gbc.roomservice.dto.RoomResponse;

import java.util.List;

public interface RoomService {
    RoomResponse createRoom( RoomRequest roomRequest );
    RoomResponse updateRoom(String id,  RoomRequest roomRequest );
    void deleteRoom(String id);
    RoomResponse getRoomById(String id);
    List<RoomResponse> getAllRooms();
    List<RoomResponse> getAvailableRooms();
    boolean checkRoomAvailability(String id);
    RoomResponse markRoomAsUnavailable(String id);
    RoomResponse markRoomAsAvailable(String id);
}
