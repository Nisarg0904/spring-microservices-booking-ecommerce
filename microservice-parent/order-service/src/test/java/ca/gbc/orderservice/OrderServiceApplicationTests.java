package ca.gbc.orderservice;

import ca.gbc.orderservice.client.InventoryClient;
import ca.gbc.orderservice.stub.InventoryClientStub;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.hamcrest.CoreMatchers.equalTo;

@AutoConfigureWireMock(port = 0)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderServiceApplicationTests {

    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest");
//			.withDatabaseName("order-service")
//			.withUsername("admin")
//			.withPassword("password");

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    static {
        postgresContainer.start();
    }

    @Test
    void createOrderTest() {
        String requestBody = """
        {
            "skuCode": "SKU0001",
            "price": "100.00",
            "quantity": 5,
            "userDetails": {
                "email": "test@example.com",
                "firstName": "John",
                "lastName": "Doe"
            }
        }
        """;

        // Mock a call to inventory-service
        InventoryClientStub.stubInventoryCall("SKU0001", 5);

        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/order")
                .then()
                .log().all()
                .statusCode(201)
                .body(equalTo("Order Placed Successfully!"));
    }
}