package ca.gbc.bookingservice.controller;

import ca.gbc.bookingservice.dto.BookingRequest;
import ca.gbc.bookingservice.dto.BookingResponse;
import ca.gbc.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    // Create a new booking
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest bookingRequest) {
        BookingResponse bookingResponse = bookingService.createBooking(bookingRequest);
        return new ResponseEntity<>(bookingResponse, HttpStatus.CREATED);
    }

    // Get all bookings
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<BookingResponse> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    // Get a booking by ID
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable("id") String bookingId) {
        BookingResponse bookingResponse = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(bookingResponse);
    }

    // Delete a booking by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookingById(@PathVariable("id") String bookingId) {
        bookingService.deleteBookingById(bookingId);
        return ResponseEntity.noContent().build();
    }

    // Check room availability
//    @GetMapping("/check-availability")
//    public ResponseEntity<Boolean> checkRoomAvailability(
//            @RequestParam("roomId") String roomId,
//            @RequestParam("startTime") String startTime,
//            @RequestParam("endTime") String endTime) {
//
//        boolean isAvailable = bookingService.isRoomAvailable(roomId, startTime, endTime);
//        return ResponseEntity.ok(isAvailable);
//    }
}
