package ca.gbc.eventservice.client;

import ca.gbc.eventservice.dto.UserResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface UserClient {
    Logger log = LoggerFactory.getLogger(UserClient.class);

    @GetExchange("/api/users/{id}")
    @CircuitBreaker(name = "user", fallbackMethod = "fallbackGetUser")
    @Retry(name = "user")
    UserResponse getUser(@PathVariable("id") String id);

    default UserResponse fallbackGetUser(String id, Throwable throwable) {
        log.error("Failed to get user for id {}, reason: {}", id, throwable.getMessage());
        return new UserResponse(id, "Unknown", "unknown@example.com", "Unknown","Unknown");
    }
}
