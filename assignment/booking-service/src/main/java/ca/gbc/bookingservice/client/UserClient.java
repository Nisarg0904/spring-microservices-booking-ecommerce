package ca.gbc.bookingservice.client;

import ca.gbc.bookingservice.dto.UserResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

@Component
public interface UserClient {
    Logger log = LoggerFactory.getLogger(UserClient.class);

    @GetExchange("/api/users/{userId}")
    @CircuitBreaker(name="user", fallbackMethod = "fallbackMethod")
    @Retry(name = "user")
    UserResponse getUserById(@PathVariable String userId);

    default UserResponse fallbackMethod(String userId, Throwable throwable) {
        log.error("Cannot fetch user by ID {}, failure reason: {}", userId, throwable.getMessage());
        return null; // Return a default UserResponse or handle it gracefully
    }

}

