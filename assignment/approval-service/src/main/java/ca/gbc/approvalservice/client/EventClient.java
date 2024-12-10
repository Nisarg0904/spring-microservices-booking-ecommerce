package ca.gbc.approvalservice.client;

import ca.gbc.approvalservice.dto.EventRequest;
import ca.gbc.approvalservice.dto.EventResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PatchExchange;

public interface EventClient {

    Logger log = LoggerFactory.getLogger(EventClient.class);

    // Fetch event details by ID
    @GetExchange("/api/events/{eventId}")
    @CircuitBreaker(name = "event", fallbackMethod = "fallbackGetEventById")
    @Retry(name = "event")
    EventResponse getEventById(@PathVariable("eventId") String eventId);

    // Update event status by ID
    @PatchExchange("/api/events/{eventId}/status")
    @CircuitBreaker(name = "event", fallbackMethod = "fallbackUpdateEventStatus")
    @Retry(name = "event")
    void updateEventStatus(@PathVariable("eventId") String eventId, @RequestBody EventRequest eventRequest);

    // Fallback methods for resilience
    default EventResponse fallbackGetEventById(String eventId, Throwable throwable) {
        log.error("Failed to fetch event details for ID {}, reason: {}", eventId, throwable.getMessage());
        throw new IllegalStateException("Failed to fetch event details for ID: " + eventId);
    }

    default void fallbackUpdateEventStatus(String eventId, EventRequest eventRequest, Throwable throwable) {
        log.error("Failed to update event status for ID {}, reason: {}", eventId, throwable.getMessage());
        throw new IllegalStateException("Failed to update event status for ID: " + eventId);
    }
}
