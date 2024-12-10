package ca.gbc.eventservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface RoomClient {
    Logger log = LoggerFactory.getLogger(RoomClient.class);

    @GetExchange("/api/rooms/capacity/{roomId}")
    @CircuitBreaker(name = "room", fallbackMethod = "fallbackGetRoomCapacity")
    @Retry(name = "room")
    Integer getRoomCapacity(@PathVariable("roomId") String roomId);

    default Integer fallbackGetRoomCapacity(String roomId, Throwable throwable) {
        log.error("Failed to get room capacity for roomId {}, reason: {}", roomId, throwable.getMessage());
        return 0; // Default fallback capacity
    }
}
