package ca.gbc.bookingservice.client;

import ca.gbc.bookingservice.dto.RoomResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import java.util.Collections;
import java.util.List;

public interface RoomClient {
    Logger log = LoggerFactory.getLogger(RoomClient.class);

    @GetExchange("/api/rooms/availability/{roomId}")
    Boolean isRoomAvailable(@PathVariable String roomId);

    default Boolean fallbackMethod(String roomId, Throwable throwable) {
        log.error("Cannot check availability for roomId {}, failure reason: {}", roomId, throwable.getMessage());
        return false;
    }

    @GetExchange("/api/rooms/availablecap/{capacity}")
    List<String> getAvailableRoomIds(@PathVariable("capacity") int capacity);

    default List<String> fallbackGetAvailableRoomIds(int capacity, Throwable throwable) {
        log.error("Failed to fetch available room IDs for capacity {}, reason: {}", capacity, throwable.getMessage());
        return Collections.emptyList(); // Return empty list as fallback
    }
}
