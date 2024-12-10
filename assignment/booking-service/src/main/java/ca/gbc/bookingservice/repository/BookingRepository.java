package ca.gbc.bookingservice.repository;

import ca.gbc.bookingservice.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findByRoomIdAndStartTimeBetweenOrEndTimeBetween(
            String roomId,
            LocalDateTime newStartTime, LocalDateTime newEndTime,
            LocalDateTime newStartTime2, LocalDateTime newEndTime2);

    Booking findByRoomIdAndStartTimeAndEndTime(String roomId,
                                                     LocalDateTime newStartTime,
                                                     LocalDateTime newEndTime);
    List<Booking> findByRoomId(String roomId);
    List<Booking> findByUserId(String userId);
}
