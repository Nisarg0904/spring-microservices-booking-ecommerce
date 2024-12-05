package ca.gbc.eventservice.stub;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class BookingClientStub {

    public static void stubMakeBookingCall(String bookingId) {
        stubFor(post(urlPathEqualTo("/api/bookings"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"" + bookingId + "\"}")));
    }

    public static void stubDeleteBookingCall(String bookingId) {
        stubFor(delete(urlPathEqualTo("/api/bookings/" + bookingId))
                .willReturn(aResponse()
                        .withStatus(204)));
    }
}
