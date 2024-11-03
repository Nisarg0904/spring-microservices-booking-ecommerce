package ca.gbc.bookingservice;

import ca.gbc.bookingservice.dto.BookingRequest;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
class BookingServiceApplicationTests {

	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

	@LocalServerPort
	private Integer port;

	@Autowired
	private RestTemplate restTemplate;

	private MockRestServiceServer mockServer;

	private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

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
	static {
		mongoDBContainer.start();
	}

	@Test
	void createBookingTest() {
		String userId = "user123";
		String roomId = "room123";
		String startTime = LocalDateTime.now().plusDays(1).format(formatter);
		String endTime = LocalDateTime.now().plusDays(1).plusHours(2).format(formatter);

		mockServer.expect(once(), requestTo("http://localhost:8087/api/users/" + userId))
				.andRespond(withStatus(HttpStatus.OK).contentType(APPLICATION_JSON));

		mockServer.expect(once(), requestTo("http://localhost:8086/api/rooms/" + roomId + "/availability"))
				.andRespond(withStatus(HttpStatus.OK)
						.contentType(APPLICATION_JSON)
						.body("true"));


		String requestBody = String.format("""
                {
                    "id": null,
                    "userId": "%s",
                    "roomId": "%s",
                    "startTime": "%s",
                    "endTime": "%s",
                    "purpose": "Team Meeting"
                }
                """, userId, roomId, startTime, endTime);

		RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.post("/api/bookings")
				.then()
				.statusCode(201)
				.body("userId", Matchers.equalTo(userId))
				.body("roomId", Matchers.equalTo(roomId))
				.body("purpose", Matchers.equalTo("Team Meeting"));
	}

	@Test
	void getAllBookingsTest() {
		String userId = "user123";
		String roomId = "room123";
		String startTime = LocalDateTime.now().plusDays(1).format(formatter);
		String endTime = LocalDateTime.now().plusDays(1).plusHours(2).format(formatter);

		mockServer.expect(once(), requestTo("http://localhost:8087/api/users/" + userId))
				.andRespond(withStatus(HttpStatus.OK).contentType(APPLICATION_JSON));

		mockServer.expect(once(), requestTo("http://localhost:8086/api/rooms/" + roomId + "/availability"))
				.andRespond(withStatus(HttpStatus.OK)
						.contentType(APPLICATION_JSON)
						.body("true"));


		String requestBody = String.format("""
                {
                    "id": null,
                    "userId": "%s",
                    "roomId": "%s",
                    "startTime": "%s",
                    "endTime": "%s",
                    "purpose": "Team Meeting"
                }
                """, userId, roomId, startTime, endTime);
		RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.post("/api/bookings")
				.then()
				.statusCode(201)
				.body("userId", Matchers.equalTo(userId))
				.body("roomId", Matchers.equalTo(roomId))
				.body("purpose", Matchers.equalTo("Team Meeting"));

		RestAssured.given()
				.contentType("application/json")
				.get("/api/bookings")
				.then()
				.statusCode(200)
				.body("size()", Matchers.greaterThan(0))
				.body("[0].userId", Matchers.equalTo(userId))
				.body("[0].roomId", Matchers.equalTo(roomId))
				.body("[0].purpose", Matchers.equalTo("Team Meeting"));
	}

	@Test
	void getBookingByIdTest() {
		// Create a booking first
		String userId = "user123";
		String roomId = "room123";
		String startTime = LocalDateTime.now().plusDays(2).format(formatter);
		String endTime = LocalDateTime.now().plusDays(2).plusHours(2).format(formatter);

		mockServer.expect(once(), requestTo("http://localhost:8087/api/users/" + userId))
				.andRespond(withStatus(HttpStatus.OK).contentType(APPLICATION_JSON));

		mockServer.expect(once(), requestTo("http://localhost:8086/api/rooms/" + roomId + "/availability"))
				.andRespond(withStatus(HttpStatus.OK)
						.contentType(APPLICATION_JSON)
						.body("true"));


		String requestBody = String.format("""
                {
                    "id": null,
                    "userId": "%s",
                    "roomId": "%s",
                    "startTime": "%s",
                    "endTime": "%s",
                    "purpose": "Workshop"
                }
                """, userId, roomId, startTime, endTime);

		String bookingId = RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.post("/api/bookings")
				.then()
				.statusCode(201)
				.extract().path("id");

		RestAssured.given()
				.contentType("application/json")
				.get("/api/bookings/" + bookingId)
				.then()
				.statusCode(200)
				.body("id", Matchers.equalTo(bookingId))
				.body("purpose", Matchers.equalTo("Workshop"));
	}

	@Test
	void deleteBookingTest() {
		String userId = "user123";
		String roomId = "room123";
		String startTime = LocalDateTime.now().plusDays(3).format(formatter);
		String endTime = LocalDateTime.now().plusDays(3).plusHours(2).format(formatter);

		mockServer.expect(once(), requestTo("http://localhost:8087/api/users/" + userId))
				.andRespond(withStatus(HttpStatus.OK).contentType(APPLICATION_JSON));

		mockServer.expect(once(), requestTo("http://localhost:8086/api/rooms/" + roomId + "/availability"))
				.andRespond(withStatus(HttpStatus.OK)
						.contentType(APPLICATION_JSON)
						.body("true"));


		String requestBody = String.format("""
                {
                    "id": null,
                    "userId": "%s",
                    "roomId": "%s",
                    "startTime": "%s",
                    "endTime": "%s",
                    "purpose": "Interview"
                }
                """, userId, roomId, startTime, endTime);

		String bookingId = RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.post("/api/bookings")
				.then()
				.statusCode(201)
				.extract().path("id");

		RestAssured.given()
				.delete("/api/bookings/" + bookingId)
				.then()
				.statusCode(204);

		RestAssured.given()
				.get("/api/bookings/" + bookingId)
				.then()
				.statusCode(404);
	}

	@Test
	void checkRoomAvailabilityTest() {
		String roomId = "room123";
		String startTime = LocalDateTime.now().plusDays(4).format(formatter);
		String endTime = LocalDateTime.now().plusDays(4).plusHours(2).format(formatter);

		mockServer.expect(once(), requestTo("http://localhost:8086/api/rooms/" + roomId + "/availability"))
				.andRespond(withStatus(HttpStatus.OK)
						.contentType(APPLICATION_JSON)
						.body("true"));


		RestAssured.given()
				.queryParam("roomId", roomId)
				.queryParam("startTime", startTime)
				.queryParam("endTime", endTime)
				.get("/api/bookings/check-availability")
				.then()
				.statusCode(200)
				.body(Matchers.equalTo("true"));
	}
}
