package ca.gbc.bookingservice.service;

import ca.gbc.bookingservice.dto.BookingRequest;
import ca.gbc.bookingservice.dto.BookingResponse;

import java.util.List;
import java.util.Optional;

public interface BookingService {
    // Create a new booking
    BookingResponse createBooking(BookingRequest bookingRequest);

    // Get all booking
    List<BookingResponse> getAllBookings();

    // Get booking by ID
    Optional<BookingResponse> getBookingById(String bookingId);

    // Check if room is available for the given time range
    boolean isRoomAvailable(String roomId, String startTime, String endTime);

    // Delete booking by ID
    void deleteBookingById(String bookingId);
}
