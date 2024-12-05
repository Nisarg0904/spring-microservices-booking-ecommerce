package ca.gbc.bookingservice.service;

import ca.gbc.bookingservice.client.RoomClient;
import ca.gbc.bookingservice.client.UserClient;
import ca.gbc.bookingservice.dto.BookingRequest;
import ca.gbc.bookingservice.dto.BookingResponse;
import ca.gbc.bookingservice.model.Booking;
import ca.gbc.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {


    private final RestTemplate restTemplate;

    private final RoomClient roomClient ;

    private final UserClient userClient ;

    private final BookingRepository bookingRepository;

    private BookingResponse mapToBookingResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getRoomId(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose()
        );
    }

    @Override
    public BookingResponse createBooking(BookingRequest bookingRequest) {
        if (!isUserValid(bookingRequest.userId())) {
            throw new IllegalStateException("User does not exist");
        }
        if (!isRoomAvailable(bookingRequest.roomId(), bookingRequest.startTime().toString(), bookingRequest.endTime().toString())) {
            throw new IllegalStateException("Room is not available for the selected time range");
        }
        Booking booking = Booking.builder()
                .userId(bookingRequest.userId())
                .roomId(bookingRequest.roomId())
                .startTime(bookingRequest.startTime())
                .endTime(bookingRequest.endTime())
                .purpose(bookingRequest.purpose())
                .build();

        bookingRepository.save(booking);

        return mapToBookingResponse(booking);

    }

    @Override
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BookingResponse> getBookingById(String bookingId) {
        return bookingRepository.findById(bookingId)
                .map(this::mapToBookingResponse);  // Map found booking to response
    }


    @Override
    public void deleteBookingById(String bookingId) {
        bookingRepository.deleteById(bookingId);
    }




    @Override
    public boolean isRoomAvailable(String roomId, String startTime, String endTime) {
        try {
            Boolean isAvailable = roomClient.isRoomAvailable(roomId);
            log.info("Room Service responded for roomId {}: {}", roomId, isAvailable);

            if (Boolean.TRUE.equals(isAvailable)) {
                LocalDateTime start = LocalDateTime.parse(startTime);
                LocalDateTime end = LocalDateTime.parse(endTime);
                List<Booking> conflictingBookings = bookingRepository.findByRoomIdAndStartTimeBetweenOrEndTimeBetween(
                        roomId, start, end, start, end);
                Booking booking = bookingRepository.findByRoomIdAndStartTimeAndEndTime(roomId, start, end);
                if (booking != null) {
                    conflictingBookings.add(booking);
                }
                return conflictingBookings.isEmpty();
            }
        } catch (Exception e) {
            log.error("Error communicating with Room Service: {}", e.getMessage());
        }
        return false;
    }


    public boolean isUserValid(String userId) {
        try {
            userClient.getUserById(userId); // If the user exists, no exception will be thrown
            return true;
        } catch (Exception e) {
            log.error("Error validating userId {}: {}", userId, e.getMessage());
            return false; // User does not exist or an error occurred
        }
    }



}
