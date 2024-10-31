package ca.gbc.userservice.repository;

import ca.gbc.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByRole(String role);

    List<User> findByUserType(String userType);

    User findByEmail(String email);
    User findByEmailAndPassword(String email, String password);
}
