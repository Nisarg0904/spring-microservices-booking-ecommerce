package ca.gbc.eventservice.stub;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class UserClientStub {

    public static void stubUserTypeCall(String userId, String userType) {
        stubFor(get(urlPathEqualTo("/api/users/type/"+ userId ))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("\"" + userType + "\"")));
    }
}
