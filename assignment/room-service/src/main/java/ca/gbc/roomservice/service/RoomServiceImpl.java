package ca.gbc.roomservice.service;

import ca.gbc.roomservice.dto.RoomRequest;
import ca.gbc.roomservice.dto.RoomResponse;
import ca.gbc.roomservice.model.Room;
import ca.gbc.roomservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    @Override
    public RoomResponse createRoom(RoomRequest roomRequest) {
        log.info("Creating a new room with name: {}", roomRequest.roomName());
        Room room = Room.builder()
                .id(roomRequest.id())
                .roomName(roomRequest.roomName())
                .capacity(roomRequest.capacity())
                .features(roomRequest.features())
                .availability(roomRequest.availability())
                .build();

        Room savedRoom = roomRepository.save(room);
        log.info("Room with ID {} created successfully", savedRoom.getId());

        return new RoomResponse(room.getId(),room.getRoomName(),room.getCapacity(),room.getFeatures(),room.isAvailability());
    }

    @Override
    public RoomResponse updateRoom(String id, RoomRequest roomRequest) {
        log.info("Updating room with ID: {}", id);
        Optional<Room> existingRoomOpt = roomRepository.findById(id);

        if (existingRoomOpt.isPresent()) {
            Room room = existingRoomOpt.get();
            room.setRoomName(roomRequest.roomName());
            room.setCapacity(roomRequest.capacity());
            room.setFeatures(roomRequest.features());
            room.setAvailability(roomRequest.availability());

            Room updatedRoom = roomRepository.save(room);
            log.info("Room with ID {} updated successfully", updatedRoom.getId());

            return mapToRoomResponse(updatedRoom);
        } else {
            log.error("Room with ID {} not found", id);
            return null;  // You can throw an exception or return a proper error response.
        }    }

    @Override
    public void deleteRoom(String id) {
        log.info("Deleting room with ID: {}", id);
        roomRepository.deleteById(id);
        log.info("Room with ID {} deleted successfully", id);

    }

    @Override
    public RoomResponse getRoomById(String id) {
        log.info("Fetching room with ID: {}", id);
        Optional<Room> roomOpt = roomRepository.findById(id);

        if (roomOpt.isPresent()) {
            log.info("Room with ID {} found", id);
            return mapToRoomResponse(roomOpt.get());
        } else {
            log.error("Room with ID {} not found", id);
            return null;
        }
    }

    @Override
    public List<RoomResponse> getAllRooms() {
        log.info("Fetching all rooms");
        List<Room> rooms = roomRepository.findAll();
        return rooms.stream().map(this::mapToRoomResponse).collect(Collectors.toList());
    }
    private RoomResponse mapToRoomResponse(Room room) {
        return new RoomResponse(room.getId(), room.getRoomName(), room.getCapacity(),
                room.getFeatures(), room.isAvailability());

    }

    @Override
    public List<RoomResponse> getAvailableRooms() {
        log.info("Fetching available rooms");
        List<Room> availableRooms = roomRepository.findByAvailability(true);
        return availableRooms.stream().map(this::mapToRoomResponse).collect(Collectors.toList());    }


    @Override
    public boolean checkRoomAvailability(String id) {
        log.info("Checking availability for room with ID: {}", id);
        Optional<Room> roomOpt = roomRepository.findById(id);

        if (roomOpt.isPresent()) {
            boolean availability = roomOpt.get().isAvailability();
            log.info("Room with ID {} is available: {}", id, availability);
            return availability;
        } else {
            log.warn("Room with ID {} not found", id);
            return false;
        }
    }

    @Override
    public RoomResponse markRoomAsUnavailable(String id) {
        log.info("Marking room with ID {} as unavailable", id);
        return changeRoomAvailability(id, false);
    }

    @Override
    public RoomResponse markRoomAsAvailable(String id) {
        log.info("Marking room with ID {} as available", id);
        return changeRoomAvailability(id, true);
    }

    @Override
    public int getRoomCapacity(String id) {
        Room room= roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + id));
        return room.getCapacity();

    }

    private RoomResponse changeRoomAvailability(String id, boolean available) {
        Optional<Room> roomOpt = roomRepository.findById(id);

        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            room.setAvailability(available);
            Room updatedRoom = roomRepository.save(room);
            log.info("Room with ID {} availability changed to {}", id, available);
            return mapToRoomResponse(updatedRoom);
        } else {
            log.error("Room with ID {} not found", id);
            return null;
        }
    }
}
