package ca.gbc.approvalservice;

import ca.gbc.approvalservice.dto.ApprovalRequest;
import ca.gbc.approvalservice.dto.ApprovalResponse;
import ca.gbc.approvalservice.model.Approval;
import ca.gbc.approvalservice.repository.ApprovalRepository;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class ApprovalServiceApplicationTests {

	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

	@Autowired
	private ApprovalRepository approvalRepository;

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
	void approveEventTest() {
		String userId = "user123";
		String eventId = "event123";

		// Mock user type
		mockServer.expect(once(), requestTo("http://localhost:8087/api/users/" + userId + "/type"))
				.andRespond(withSuccess("staff", MediaType.TEXT_PLAIN));

		// Mock GET request for retrieving event details
		mockServer.expect(once(), requestTo("http://localhost:8089/api/events/" + eventId))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"id\":\"" + eventId + "\", \"bookingId\":\"booking123\"}", MediaType.APPLICATION_JSON));

		// Mock PATCH request for updating event status
		mockServer.expect(once(), requestTo("http://localhost:8089/api/events/" + eventId + "/status"))
				.andExpect(method(HttpMethod.PATCH))
				.andRespond(withStatus(HttpStatus.NO_CONTENT));

		ApprovalRequest approvalRequest = new ApprovalRequest(userId, eventId, true, "Approved by staff");

		RestAssured.given()
				.contentType("application/json")
				.body(approvalRequest)
				.post("/api/approvals")
				.then()
				.statusCode(200)
				.body("isApproved", Matchers.equalTo(true))
				.body("comments", Matchers.equalTo("Approved by staff"));
	}

	@Test
	void rejectEventTest() {
		String userId = "user123";
		String eventId = "event123";
		String bookingId = "booking456";

		// Mock user type
		mockServer.expect(once(), requestTo("http://localhost:8087/api/users/" + userId + "/type"))
				.andRespond(withSuccess("staff", MediaType.TEXT_PLAIN));

		// Mock event status update
		mockServer.expect(once(), requestTo("http://localhost:8089/api/events/" + eventId))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"id\":\"" + eventId + "\", \"bookingId\":\"booking456\"}", MediaType.APPLICATION_JSON));

		// Mock PATCH request for updating event status
		mockServer.expect(once(), requestTo("http://localhost:8089/api/events/" + eventId + "/status"))
				.andExpect(method(HttpMethod.PATCH))
				.andRespond(withStatus(HttpStatus.NO_CONTENT));

		// Mock booking deletion
		mockServer.expect(once(), requestTo("http://localhost:8088/api/bookings/" + bookingId))
				.andExpect(method(HttpMethod.DELETE))
				.andRespond(withStatus(HttpStatus.NO_CONTENT));

		ApprovalRequest approvalRequest = new ApprovalRequest(userId, eventId, false, "Rejected by staff");

		RestAssured.given()
				.contentType("application/json")
				.body(approvalRequest)
				.post("/api/approvals")
				.then()
				.statusCode(200)
				.body("isApproved", Matchers.equalTo(false))
				.body("comments", Matchers.equalTo("Rejected by staff"));
	}

	@Test
	void getAllApprovalsTest() {
		RestAssured.given()
				.get("/api/approvals")
				.then()
				.statusCode(200)
				.body("size()", Matchers.greaterThanOrEqualTo(0));
	}
	@Test
	void getApprovalByIdTest() {
		String approvalId = "approval123";
		Approval approval = new Approval(approvalId, "user123", "event123", true, "Approved");

		// Insert the approval directly into the MongoDB instance
		approvalRepository.save(approval);

		// Now perform the test
		RestAssured.given()
				.get("/api/approvals/" + approvalId)
				.then()
				.statusCode(200)
				.body("id", Matchers.equalTo(approvalId))
				.body("isApproved", Matchers.equalTo(true));
	}
	@Test
	void getApprovalsByStatusTest() {
		RestAssured.given()
				.get("/api/approvals/status/approved")
				.then()
				.statusCode(200)
				.body("size()", Matchers.greaterThanOrEqualTo(0));

		RestAssured.given()
				.get("/api/approvals/status/rejected")
				.then()
				.statusCode(200)
				.body("size()", Matchers.greaterThanOrEqualTo(0));
	}
}
