package ca.gbc.approvalservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.DeleteExchange;

public interface BookingClient {

    Logger log = LoggerFactory.getLogger(BookingClient.class);

    // Delete a booking by ID
    @DeleteExchange("/api/bookings/{bookingId}")
    void deleteBooking(@PathVariable("bookingId") String bookingId);

    // Fallback for delete operation
    default void fallbackDeleteBooking(String bookingId, Throwable throwable) {
        log.error("Failed to delete booking with ID {}, reason: {}", bookingId, throwable.getMessage());
        throw new IllegalStateException("Booking deletion failed: " + throwable.getMessage());
    }
}
