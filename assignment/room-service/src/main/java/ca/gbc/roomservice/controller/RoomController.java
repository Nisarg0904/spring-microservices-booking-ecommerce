package ca.gbc.roomservice.controller;

import ca.gbc.roomservice.dto.RoomRequest;
import ca.gbc.roomservice.dto.RoomResponse;
import ca.gbc.roomservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;



@RequiredArgsConstructor
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    // Create a new room
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<RoomResponse> createRoom(@RequestBody RoomRequest roomRequest) {
        RoomResponse createdRoom = roomService.createRoom(roomRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/rooms/" + createdRoom.id());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createdRoom);
    }

    // Get all rooms
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<RoomResponse> getAllRooms() {
        return roomService.getAllRooms();
    }

    // Get a specific room by ID
    @GetMapping("/{roomId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable("roomId") String roomId) {
        RoomResponse roomResponse = roomService.getRoomById(roomId);
        return roomResponse != null
                ? ResponseEntity.ok(roomResponse)
                : ResponseEntity.notFound().build();
    }

    // Update a room
    @PutMapping("/{roomId}")
    public ResponseEntity<?> updateRoom(@PathVariable("roomId") String roomId, @RequestBody RoomRequest roomRequest) {
        RoomResponse updatedRoom = roomService.updateRoom(roomId, roomRequest);
        if (updatedRoom != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", "/api/rooms/" + updatedRoom.id());
            return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a room by ID
    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable("roomId") String roomId) {
        roomService.deleteRoom(roomId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    // Get available rooms
    @GetMapping("/available")
    @ResponseStatus(HttpStatus.OK)
    public List<RoomResponse> getAvailableRooms() {
        return roomService.getAvailableRooms();
    }

    @GetMapping("/availablecap/{capacity}")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getAvailableRoomIdsWithCapacity(@PathVariable("capacity") int capacity) {
        return roomService.getAvailableRoomIdsWithCapacity(capacity);
    }

    // Check if a specific room is available
    @GetMapping("/availability/{roomId}")
    public ResponseEntity<Boolean> checkRoomAvailability(@PathVariable("roomId") String roomId) {
        boolean isAvailable = roomService.checkRoomAvailability(roomId);
        return ResponseEntity.ok(isAvailable);
    }

    // Mark a room as unavailable
    @PatchMapping("/unavailable/{roomId}")
    public ResponseEntity<?> markRoomAsUnavailable(@PathVariable("roomId") String roomId) {
        RoomResponse updatedRoom = roomService.markRoomAsUnavailable(roomId);
        if (updatedRoom != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", "/api/rooms/" + updatedRoom.id());
            return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/capacity/{roomId}")
    public ResponseEntity<Integer> getRoomCapacity(@PathVariable("roomId") String roomId) {
        Integer roomCapacity = roomService.getRoomCapacity(roomId);
        return ResponseEntity.ok(roomCapacity);
    }


    // Mark a room as available
    @PatchMapping("/available/{roomId}")
    public ResponseEntity<?> markRoomAsAvailable(@PathVariable("roomId") String roomId) {
        RoomResponse updatedRoom = roomService.markRoomAsAvailable(roomId);
        if (updatedRoom != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", "/api/rooms/" + updatedRoom.id());
            return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
