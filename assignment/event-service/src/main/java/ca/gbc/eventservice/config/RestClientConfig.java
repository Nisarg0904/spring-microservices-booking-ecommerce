package ca.gbc.eventservice.config;

import ca.gbc.eventservice.client.RoomClient;
import ca.gbc.eventservice.client.UserClient;
import ca.gbc.eventservice.client.BookingClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${room.service.url}")
    private String roomServiceUrl;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Value("${booking.service.url}")
    private String bookingServiceUrl;



    private <T> T createClient(Class<T> clientType, String baseUrl) {
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(getClientRequestFactory())
                .build();

        var restClientAdapter = RestClientAdapter.create(restClient);
        var httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(restClientAdapter).build();

        return httpServiceProxyFactory.createClient(clientType);
    }

    @Bean
    public RoomClient roomClient() {
        return createClient(RoomClient.class, roomServiceUrl);
    }

    @Bean
    public UserClient userClient() {
        return createClient(UserClient.class, userServiceUrl);
    }

    @Bean
    public BookingClient bookingClient() {
        return createClient(BookingClient.class, bookingServiceUrl);
    }

    private ClientHttpRequestFactory getClientRequestFactory() {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(3))
                .withReadTimeout(Duration.ofSeconds(3));
        return ClientHttpRequestFactories.get(settings);
    }
}
