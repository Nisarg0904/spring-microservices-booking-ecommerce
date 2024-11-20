package ca.gbc.inventoryservice;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class InventoryServiceApplicationTests {

	@ServiceConnection
	static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
			.withDatabaseName("inventory")
			.withUsername("admin")
			.withPassword("password");

	@LocalServerPort
	private Integer port;

	@BeforeEach
	void setup() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}

	static {
		postgreSQLContainer.start();
	}

	@Test
	void isInStockTest() {
		String skuCode = "SKU001";
		int quantity = 10;

		// Assuming the database is pre-populated with this inventory item for testing.
		RestAssured.given()
				.param("skuCode", skuCode)
				.param("quantity", quantity)
				.when()
				.get("/api/inventory")
				.then()
				.log().all()
				.statusCode(200)
				.body(equalTo("true"));  // Adjust based on your expected response (true or false)
	}
}
