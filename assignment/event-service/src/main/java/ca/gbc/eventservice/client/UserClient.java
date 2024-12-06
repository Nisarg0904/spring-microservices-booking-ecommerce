package ca.gbc.eventservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface UserClient {
    Logger log = LoggerFactory.getLogger(UserClient.class);

    @GetExchange("/api/users/type/{userId}")
    @CircuitBreaker(name = "user", fallbackMethod = "fallbackGetUserType")
    @Retry(name = "user")
    String getUserType(@PathVariable("userId") String userId);

    default String fallbackGetUserType(String userId, Throwable throwable) {
        log.error("Failed to get user type for userId {}, reason: {}", userId, throwable.getMessage());
        return "Unknown"; // Default fallback value
    }
}
