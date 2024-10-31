package ca.gbc.userservice;
import ca.gbc.userservice.dto.UserRequest;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserServiceApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @LocalServerPort
    private Integer port;

    static {
        postgreSQLContainer.start();
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void createUserTest() {
        String requestBody = """
                {
                "id": null,
                "name": "John Doe",
                "email": "johndoe@example.com",
                "password": "password123",
                "role": "USER",
                "userType": "student"
                }
                """;

        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .post("/api/users")
                .then()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("name", Matchers.equalTo("John Doe"))
                .body("email", Matchers.equalTo("johndoe@example.com"))
                .body("role", Matchers.equalTo("USER"))
                .body("userType", Matchers.equalTo("student"));
    }

    @Test
    void createUserWithDuplicateEmailTest() {
        String requestBody = """
                {
                "id": null,
                "name": "Jane Doe",
                "email": "janedoe@example.com",
                "password": "password123",
                "role": "USER",
                "userType": "faculty"
                }
                """;

        // Create the first user
        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .post("/api/users")
                .then()
                .statusCode(201);

        // Attempt to create a second user with the same email
        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .post("/api/users")
                .then()
                .statusCode(400);  // Expect a 400 Bad Request due to duplicate email
    }

    @Test
    void updateUserWithoutEmailChangeTest() {
        String requestBody = """
                {
                "id": null,
                "name": "Alice Doe",
                "email": "alicedoe@example.com",
                "password": "password123",
                "role": "USER",
                "userType": "staff"
                }
                """;

        // Create the user
        Long userId = ((Integer) RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .post("/api/users")
                .then()
                .statusCode(201)
                .extract().path("id")).longValue();

        String updateRequestBody = """
                {
                "id": null,
                "name": "Alice Smith",
                "email": "alicedoe@example.com",
                "password": "newpassword",
                "role": "ADMIN",
                "userType": "staff"
                }
                """;

        // Update the user's name, password, role, and userType, but not email
        RestAssured.given()
                .contentType("application/json")
                .body(updateRequestBody)
                .put("/api/users/" + userId)
                .then()
                .statusCode(200)
                .body("name", Matchers.equalTo("Alice Smith"))
                .body("email", Matchers.equalTo("alicedoe@example.com"))  // Email remains unchanged
                .body("role", Matchers.equalTo("ADMIN"))
                .body("userType", Matchers.equalTo("staff"));
    }

    @Test
    void deleteUserTest() {
        String requestBody = """
                {
                "id": null,
                "name": "Bob Doe",
                "email": "bobdoe@example.com",
                "password": "password123",
                "role": "USER",
                "userType": "student"
                }
                """;

        // Create the user
        Long userId = ((Integer) RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .post("/api/users")
                .then()
                .statusCode(201)
                .extract().path("id")).longValue();

        // Delete the user
        RestAssured.given()
                .delete("/api/users/" + userId)
                .then()
                .statusCode(204);

        // Verify the user no longer exists
        RestAssured.given()
                .get("/api/users/" + userId)
                .then()
                .statusCode(404);
    }

    @Test
    void signInTest() {
        String requestBody = """
                {
                "id": null,
                "name": "Charlie Doe",
                "email": "charliedoe@example.com",
                "password": "mypassword",
                "role": "USER",
                "userType": "faculty"
                }
                """;

        // Create the user
        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .post("/api/users")
                .then()
                .statusCode(201);

        // Attempt to sign in with correct email and password
        RestAssured.given()
                .queryParam("email", "charliedoe@example.com")
                .queryParam("password", "mypassword")
                .post("/api/users/signin")
                .then()
                .statusCode(200)
                .body("name", Matchers.equalTo("Charlie Doe"))
                .body("email", Matchers.equalTo("charliedoe@example.com"))
                .body("role", Matchers.equalTo("USER"))
                .body("userType", Matchers.equalTo("faculty"));
    }

    @Test
    void signInWithIncorrectCredentialsTest() {
        String requestBody = """
                {
                "id": null,
                "name": "Dana Doe",
                "email": "danadoe@example.com",
                "password": "securepassword",
                "role": "USER",
                "userType": "staff"
                }
                """;

        // Create the user
        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .post("/api/users")
                .then()
                .statusCode(201);

        // Attempt to sign in with incorrect password
        RestAssured.given()
                .queryParam("email", "danadoe@example.com")
                .queryParam("password", "wrongpassword")
                .post("/api/users/signin")
                .then()
                .statusCode(401);  // Expect 401 Unauthorized
    }
}