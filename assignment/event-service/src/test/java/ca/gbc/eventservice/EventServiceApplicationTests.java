package ca.gbc.eventservice;

import ca.gbc.eventservice.stub.BookingClientStub;
import ca.gbc.eventservice.stub.RoomClientStub;
import ca.gbc.eventservice.stub.UserClientStub;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import static org.hamcrest.Matchers.equalTo;

@AutoConfigureWireMock(port = 0)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventServiceApplicationTests {

	@LocalServerPort
	private Integer port;

	@BeforeEach
	void setUp() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}

	@Test
	void createEventTest() {
		String organizerId = "user123";
		String roomId = "room123";
		String bookingId = "booking123";
		String startTime = "2024-12-06T10:00:00";
		String endTime = "2024-12-06T12:00:00";

		// Stub external service calls
		UserClientStub.stubUserTypeCall(organizerId, "student");
		RoomClientStub.stubRoomCapacityCall(roomId, 50);
		BookingClientStub.stubMakeBookingCall(bookingId);

		String requestBody = String.format("""
            {
                "eventName": "Coding Workshop",
                "organizerId": "%s",
                "eventType": "Workshop",
                "expectedAttendees": 20,
                "roomId": "%s",
                "startTime": "%s",
                "endTime": "%s"
            }
            """, organizerId, roomId, startTime, endTime);

		RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.post("/api/events")
				.then()
				.statusCode(201)
				.body("eventName", equalTo("Coding Workshop"))
				.body("organizerId", equalTo(organizerId))
				.body("bookingId", equalTo(bookingId));
	}

	@Test
	void getAllEventsTest() {
		RestAssured.given()
				.contentType("application/json")
				.get("/api/events")
				.then()
				.statusCode(200)
				.body("size()", equalTo(0)); // Update the expectation based on your actual data
	}

	@Test
	void getEventByIdTest() {
		String organizerId = "user123";
		String roomId = "room123";
		String bookingId = "booking123";
		String eventId = "event123";

		UserClientStub.stubUserTypeCall(organizerId, "staff");
		RoomClientStub.stubRoomCapacityCall(roomId, 50);
		BookingClientStub.stubMakeBookingCall(bookingId);

		RestAssured.given()
				.contentType("application/json")
				.get("/api/events/" + eventId)
				.then()
				.statusCode(200)
				.body("id", equalTo(eventId))
				.body("eventName", equalTo("Coding Workshop"));
	}

	@Test
	void deleteEventTest() {
		String bookingId = "booking123";

		BookingClientStub.stubDeleteBookingCall(bookingId);

		RestAssured.given()
				.delete("/api/events/" + bookingId)
				.then()
				.statusCode(204);
	}

	@Test
	void updateEventStatusTest() {
		String eventId = "event123";

		RestAssured.given()
				.contentType("application/json")
				.body("{\"status\":\"CONFIRMED\"}")
				.patch("/api/events/" + eventId + "/status")
				.then()
				.statusCode(204);

		RestAssured.given()
				.get("/api/events/" + eventId)
				.then()
				.statusCode(200)
				.body("status", equalTo("CONFIRMED"));
	}
}
