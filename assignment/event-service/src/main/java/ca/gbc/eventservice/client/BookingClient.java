package ca.gbc.eventservice.client;

import ca.gbc.eventservice.dto.BookingRequest;
import ca.gbc.eventservice.dto.BookingResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.PostExchange;

public interface BookingClient {
    Logger log = LoggerFactory.getLogger(BookingClient.class);

    @PostExchange("/api/bookings")
    @CircuitBreaker(name = "booking", fallbackMethod = "fallbackMakeBooking")
    @Retry(name = "booking")
    BookingResponse makeBooking(@RequestBody BookingRequest bookingRequest);

    @DeleteExchange("/api/bookings/{bookingId}")
    @CircuitBreaker(name = "booking", fallbackMethod = "fallbackDeleteBooking")
    @Retry(name = "booking")
    void deleteBooking(@PathVariable("bookingId") String bookingId);

    default BookingResponse fallbackMakeBooking(BookingRequest bookingRequest, Throwable throwable) {
        log.error("Failed to create booking for request {}, reason: {}", bookingRequest, throwable.getMessage());
        throw new IllegalStateException("Failed to create booking: " + throwable.getMessage());
    }

    default void fallbackDeleteBooking(String bookingId, Throwable throwable) {
        log.error("Failed to delete booking with bookingId {}, reason: {}", bookingId, throwable.getMessage());
        throw new IllegalStateException("Failed to delete booking: " + throwable.getMessage());
    }
}
