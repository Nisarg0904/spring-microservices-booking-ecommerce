package ca.gbc.roomservice;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoomServiceApplicationTests {

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
    void createRoomTest() {
        String requestBody = """
                {
                "id": "1",
                "roomName": "Conference Room",
                "capacity": 20,
                "features": "Projector, Whiteboard",
                "availability": true
                }
                """;

        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/rooms")
                .then()
                .log().all()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("roomName", Matchers.equalTo("Conference Room"))
                .body("capacity", Matchers.equalTo(20))
                .body("features", Matchers.equalTo("Projector, Whiteboard"))
                .body("availability", Matchers.equalTo(true));
    }

    @Test
    void getAllRoomsTest() {
        String requestBody = """
                {
                "id": "1",
                "roomName": "Conference Room",
                "capacity": 20,
                "features": "Projector, Whiteboard",
                "availability": true
                }
                """;

        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/rooms")
                .then()
                .statusCode(201);

        RestAssured.given()
                .contentType("application/json")
                .when()
                .get("/api/rooms")
                .then()
                .log().all()
                .statusCode(200)
                .body("size()", Matchers.greaterThan(0))
                .body("[0].id", Matchers.notNullValue())
                .body("[0].roomName", Matchers.equalTo("Conference Room"))
                .body("[0].capacity", Matchers.equalTo(20))
                .body("[0].features", Matchers.equalTo("Projector, Whiteboard"))
                .body("[0].availability", Matchers.equalTo(true));
    } @Test
    void getRoomByIdTest() {
        String requestBody = """
                {
                "id": "1",
                "roomName": "Conference Room",
                "capacity": 20,
                "features": "Projector, Whiteboard",
                "availability": true
                }
                """;

        RestAssured.given().contentType("application/json").body(requestBody)
                .post("/api/rooms")
                .then().statusCode(201);

        RestAssured.given().contentType("application/json")
                .get("/api/rooms/1")
                .then().log().all().statusCode(200)
                .body("id", Matchers.equalTo("1"))
                .body("roomName", Matchers.equalTo("Conference Room"));
    }

    @Test
    void updateRoomTest() {
        String requestBody = """
                {
                "id": "2",
                "roomName": "Meeting Room",
                "capacity": 15,
                "features": "Whiteboard",
                "availability": true
                }
                """;

        String updateRequestBody = """
                {
                "id": "2",
                "roomName": "Updated Meeting Room",
                "capacity": 25,
                "features": "Whiteboard, TV",
                "availability": false
                }
                """;

        RestAssured.given().contentType("application/json").body(requestBody)
                .post("/api/rooms")
                .then().statusCode(201);

        RestAssured.given().contentType("application/json").body(updateRequestBody)
                .put("/api/rooms/2")
                .then().statusCode(204);

        RestAssured.given().contentType("application/json")
                .get("/api/rooms/2")
                .then().statusCode(200)
                .body("roomName", Matchers.equalTo("Updated Meeting Room"))
                .body("capacity", Matchers.equalTo(25))
                .body("availability", Matchers.equalTo(false));
    }

    @Test
    void deleteRoomTest() {
        String requestBody = """
                {
                "id": "3",
                "roomName": "Training Room",
                "capacity": 30,
                "features": "TV, Whiteboard",
                "availability": true
                }
                """;

        RestAssured.given().contentType("application/json").body(requestBody)
                .post("/api/rooms")
                .then().statusCode(201);

        RestAssured.given().delete("/api/rooms/3")
                .then().statusCode(204);

        RestAssured.given().get("/api/rooms/3")
                .then().statusCode(404);
    }

    @Test
    void getAvailableRoomsTest() {
        String requestBody = """
                {
                "id": "4",
                "roomName": "Available Room",
                "capacity": 10,
                "features": "Projector",
                "availability": true
                }
                """;

        RestAssured.given().contentType("application/json").body(requestBody)
                .post("/api/rooms")
                .then().statusCode(201);

        RestAssured.given().get("/api/rooms/available")
                .then().statusCode(200)
                .body("size()", Matchers.greaterThan(0))
                .body("[0].id", Matchers.equalTo("4"))
                .body("[0].availability", Matchers.equalTo(true));
    }

    @Test
    void checkRoomAvailabilityTest() {
        String requestBody = """
                {
                "id": "5",
                "roomName": "Check Room",
                "capacity": 15,
                "features": "TV",
                "availability": true
                }
                """;

        RestAssured.given().contentType("application/json").body(requestBody)
                .post("/api/rooms")
                .then().statusCode(201);

        RestAssured.given().get("/api/rooms/5/availability")
                .then().statusCode(200)
                .body(Matchers.equalTo("true"));
    }

    @Test
    void markRoomAsUnavailableTest() {
        String requestBody = """
                {
                "id": "6",
                "roomName": "Unavailable Room",
                "capacity": 20,
                "features": "TV, Projector",
                "availability": true
                }
                """;

        RestAssured.given().contentType("application/json").body(requestBody)
                .post("/api/rooms")
                .then().statusCode(201);

        RestAssured.given().patch("/api/rooms/6/unavailable")
                .then().statusCode(204);

        RestAssured.given().get("/api/rooms/6")
                .then().statusCode(200)
                .body("availability", Matchers.equalTo(false));
    }

    @Test
    void markRoomAsAvailableTest() {
        String requestBody = """
                {
                "id": "7",
                "roomName": "Available Room",
                "capacity": 25,
                "features": "TV",
                "availability": false
                }
                """;

        RestAssured.given().contentType("application/json").body(requestBody)
                .post("/api/rooms")
                .then().statusCode(201);

        RestAssured.given().patch("/api/rooms/7/available")
                .then().statusCode(204);

        RestAssured.given().get("/api/rooms/7")
                .then().statusCode(200)
                .body("availability", Matchers.equalTo(true));
    }

    @Test
    void getRoomCapacityTest() {
        String requestBody = """
                {
                "id": "8",
                "roomName": "Capacity Room",
                "capacity": 50,
                "features": "Whiteboard",
                "availability": true
                }
                """;

        RestAssured.given().contentType("application/json").body(requestBody)
                .post("/api/rooms")
                .then().statusCode(201);

        RestAssured.given().get("/api/rooms/8/capacity")
                .then().statusCode(200)
                .body(Matchers.equalTo("50"));
    }
}