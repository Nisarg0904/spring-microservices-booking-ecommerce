package ca.gbc.eventservice.stub;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class RoomClientStub {

    public static void stubRoomCapacityCall(String roomId, int capacity) {
        stubFor(get(urlPathEqualTo("/api/rooms/capacity/"+ roomId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(String.valueOf(capacity))));
    }
}
