package ca.gbc.eventservice.client;

import ca.gbc.eventservice.dto.BookingRequest;
import ca.gbc.eventservice.dto.BookingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.bind.annotation.PathVariable;


public interface BookingClient {
    Logger log = LoggerFactory.getLogger(BookingClient.class);

    @PostExchange("/api/bookings")
    BookingResponse makeBooking(@RequestBody BookingRequest bookingRequest);

    @DeleteExchange("/api/bookings/{bookingId}")
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
