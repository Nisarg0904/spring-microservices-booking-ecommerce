package ca.gbc.roomservice.repository;

import ca.gbc.roomservice.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {
    List<Room> findByAvailability(boolean availability);
    List<Room> findByAvailabilityAndCapacityGreaterThanEqual(boolean availability,  int capacity);

}
