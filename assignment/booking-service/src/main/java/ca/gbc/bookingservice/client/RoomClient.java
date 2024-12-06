package ca.gbc.bookingservice.client;

import ca.gbc.bookingservice.dto.RoomResponse;

import groovy.util.logging.Slf4j;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.service.annotation.GetExchange;

import java.util.Collections;
import java.util.List;


//@Slf4j
public interface RoomClient {
    Logger log = LoggerFactory.getLogger(RoomClient.class);

    @GetExchange("/api/rooms/availability/{roomId}")
    @CircuitBreaker(name = "room", fallbackMethod = "fallbackIsRoomAvailable")
    @Retry(name = "room")
    Boolean isRoomAvailable(@PathVariable("roomId") String roomId);

    default Boolean fallbackIsRoomAvailable(String roomId, Throwable throwable) {
        log.error("Cannot check availability for roomId {}, failure reason: {}", roomId, throwable.getMessage());
        return false; // Return false as a fallback
    }

    @GetExchange("/api/rooms/availablecap/{capacity}")
    @CircuitBreaker(name = "room", fallbackMethod = "fallbackGetAvailableRoomIds")
    @Retry(name = "room")
    List<String> getAvailableRoomIds(@PathVariable("capacity") int capacity);

    default List<String> fallbackGetAvailableRoomIds(int capacity, Throwable throwable) {
        log.error("Failed to fetch available room IDs for capacity {}, reason: {}", capacity, throwable.getMessage());
        return Collections.emptyList(); // Return empty list as fallback
    }
}
