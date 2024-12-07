package ca.gbc.bookingservice.client;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.service.annotation.GetExchange;

import java.util.Collections;
import java.util.List;


@Component
public interface RoomClient {
    Logger log = LoggerFactory.getLogger(RoomClient.class);

    @GetExchange("/api/rooms/availability/{roomId}")
    @CircuitBreaker(name = "room", fallbackMethod = "fallbackIsRoomAvailable")
    @Retry(name = "room")
    boolean isRoomAvailable(@PathVariable("roomId") String roomId);

    default boolean fallbackIsRoomAvailable(String roomId, Throwable throwable) {
        log.error("Cannot check availability for roomId {}, failure reason: {}", roomId, throwable.getMessage());
        return false;
    }


    @GetExchange("/api/rooms/availablecap/{capacity}")
    @CircuitBreaker(name = "room", fallbackMethod = "fallbackGetAvailableRoomIds")
    @Retry(name = "room")
    List<String> getAvailableRoomIds(@PathVariable("capacity") int capacity);

    default List<String> fallbackGetAvailableRoomIds(int capacity, Throwable throwable) {
        log.error("Failed to fetch room IDs for capacity {}, failure reason: {}", capacity, throwable.getMessage());
        return Collections.emptyList();
    }


}
