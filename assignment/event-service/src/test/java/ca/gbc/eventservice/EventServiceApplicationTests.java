package ca.gbc.eventservice;

import ca.gbc.eventservice.dto.EventRequest;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.assertEquals;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
class EventServiceApplicationTests {

	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

	@LocalServerPort
	private Integer port;

	@Autowired
	private RestTemplate restTemplate;

	private MockRestServiceServer mockServer;
	private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

	static {
		mongoDBContainer.start();
	}
	@DynamicPropertySource
	static void setUpMongoDBProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@BeforeEach
	void setup() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
		mockServer = MockRestServiceServer.createServer(restTemplate);
	}
	@Test
	void createEventTest() {
		String organizerId = "user123";
		String roomId = "room123";
		String startTime = LocalDateTime.now().plusDays(1).format(formatter);
		String endTime = LocalDateTime.now().plusDays(1).plusHours(2).format(formatter);

		mockServer.expect(once(), requestTo("http://localhost:8087/api/users/" + organizerId + "/type"))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body("\"student\""));

		mockServer.expect(once(), requestTo("http://localhost:8086/api/rooms/" + roomId + "/capacity"))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body("50"));

		mockServer.expect(once(), requestTo("http://localhost:8088/api/bookings"))
				.andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body("{\"id\":\"booking123\"}"));

		String requestBody = String.format("""
            {
                "id": null,
                "eventName": "Coding Workshop",
                "organizerId": "%s",
                "eventType": "Workshop",
                "expectedAttendees": 20,
                "roomId": "%s",
                "startTime": "%s",
                "endTime": "%s",
                "Status": "PENDING",
                "bookingId": null
            }
            """, organizerId, roomId, startTime, endTime);

		RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.post("/api/events")
				.then()
				.statusCode(201)
				.body("eventName", Matchers.equalTo("Coding Workshop"))
				.body("organizerId", Matchers.equalTo(organizerId))
				.body("eventType", Matchers.equalTo("Workshop"))
				.body("Status", Matchers.equalTo("PENDING"))
				.body("bookingId", Matchers.equalTo("booking123"));
	}


	@Test
	void getAllEventsTest() {
		RestAssured.given()
				.contentType("application/json")
				.get("/api/events")
				.then()
				.statusCode(200)
				.body("size()", Matchers.greaterThanOrEqualTo(0));
	}

	@Test
	void getEventByIdTest() {
		String organizerId = "user123";
		String roomId = "room123";
		String startTime = LocalDateTime.now().plusDays(1).format(formatter);
		String endTime = LocalDateTime.now().plusDays(1).plusHours(2).format(formatter);

		mockServer.expect(once(), requestTo("http://localhost:8087/api/users/" + organizerId + "/type"))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body("\"student\""));

		mockServer.expect(once(), requestTo("http://localhost:8086/api/rooms/" + roomId + "/capacity"))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body("50"));

		mockServer.expect(once(), requestTo("http://localhost:8088/api/bookings"))
				.andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body("{\"id\":\"booking123\"}"));

		String requestBody = String.format("""
            {
                "id": null,
                "eventName": "Coding Workshop",
                "organizerId": "%s",
                "eventType": "Workshop",
                "expectedAttendees": 20,
                "roomId": "%s",
                "startTime": "%s",
                "endTime": "%s",
                "Status": "PENDING",
                "bookingId": null
            }
            """, organizerId, roomId, startTime, endTime);

		String eventId = RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.post("/api/events")
				.then()
				.statusCode(201)
				.extract().path("id");

		RestAssured.given()
				.contentType("application/json")
				.get("/api/events/" + eventId)
				.then()
				.statusCode(200)
				.body("id", Matchers.equalTo(eventId))
				.body("eventName", Matchers.equalTo("Coding Workshop"));
	}

	@Test
	void deleteEventTest() {
		String organizerId = "user123";
		String roomId = "room123";
		String startTime = LocalDateTime.now().plusDays(1).format(formatter);
		String endTime = LocalDateTime.now().plusDays(1).plusHours(2).format(formatter);

		mockServer.expect(once(), requestTo("http://localhost:8087/api/users/" + organizerId + "/type"))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body("\"student\""));

		mockServer.expect(once(), requestTo("http://localhost:8086/api/rooms/" + roomId + "/capacity"))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body("50"));

		mockServer.expect(once(), requestTo("http://localhost:8088/api/bookings"))
				.andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body("{\"id\":\"booking123\"}"));

		mockServer.expect(requestTo("http://localhost:8088/api/bookings/booking123"))
				.andExpect(request -> assertEquals(HttpMethod.DELETE, request.getMethod()))
				.andRespond(withStatus(HttpStatus.NO_CONTENT));


		String requestBody = String.format("""
        {
            "id": null,
            "eventName": "Coding Workshop",
            "organizerId": "%s",
            "eventType": "Workshop",
            "expectedAttendees": 20,
            "roomId": "%s",
            "startTime": "%s",
            "endTime": "%s",
            "Status": "PENDING",
            "bookingId": null
        }
        """, organizerId, roomId, startTime, endTime);

		String eventId = RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.post("/api/events")
				.then()
				.statusCode(201)
				.extract().path("id");

		RestAssured.given()
				.delete("/api/events/" + eventId)
				.then()
				.statusCode(204);

		RestAssured.given()
				.get("/api/events/" + eventId)
				.then()
				.statusCode(404);
	}

	@Test
	void updateEventStatusTest() {
		String organizerId = "user123";
		String roomId = "room123";
		String startTime = LocalDateTime.now().plusDays(1).format(formatter);
		String endTime = LocalDateTime.now().plusDays(1).plusHours(2).format(formatter);

		mockServer.expect(once(), requestTo("http://localhost:8087/api/users/" + organizerId + "/type"))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body("\"student\""));

		mockServer.expect(once(), requestTo("http://localhost:8086/api/rooms/" + roomId + "/capacity"))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body("50"));

		mockServer.expect(once(), requestTo("http://localhost:8088/api/bookings"))
				.andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body("{\"id\":\"booking123\"}"));

		String requestBody = String.format("""
            {
                "id": null,
                "eventName": "Coding Workshop",
                "organizerId": "%s",
                "eventType": "Workshop",
                "expectedAttendees": 20,
                "roomId": "%s",
                "startTime": "%s",
                "endTime": "%s",
                "Status": "PENDING",
                "bookingId": null
            }
            """, organizerId, roomId, startTime, endTime);

		String eventId = RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.post("/api/events")
				.then()
				.statusCode(201)
				.extract().path("id");

		RestAssured.given()
				.contentType("application/json")
				.body("{\"Status\":\"CONFIRMED\"}")
				.patch("/api/events/" + eventId + "/status")
				.then()
				.statusCode(204);

 		RestAssured.given()
				.get("/api/events/" + eventId)
				.then()
				.statusCode(200)
				.body("Status", Matchers.equalTo("CONFIRMED"));
	}
}
