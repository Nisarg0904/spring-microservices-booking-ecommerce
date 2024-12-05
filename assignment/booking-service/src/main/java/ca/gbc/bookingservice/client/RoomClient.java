package ca.gbc.bookingservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface RoomClient {
    Logger log = LoggerFactory.getLogger(RoomClient.class);

    @GetExchange("/api/rooms/availability/{roomId}")
    Boolean isRoomAvailable(@PathVariable String roomId);

    default Boolean fallbackMethod(String roomId, Throwable throwable) {
        log.error("Cannot check availability for roomId {}, failure reason: {}", roomId, throwable.getMessage());
        return false;
    }
}
