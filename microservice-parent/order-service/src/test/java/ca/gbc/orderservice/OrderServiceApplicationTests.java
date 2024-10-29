package ca.gbc.orderservice;

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
class OrderServiceApplicationTests {
    @ServiceConnection
    static PostgreSQLContainer postgreSQLContainer=new PostgreSQLContainer("postgres:latest");

    @LocalServerPort
    private Integer port;


    @BeforeEach
    void setup(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }
    static {
        postgreSQLContainer.start();
    }

 @Test
    void createOrderTest() {

     String requestBody= """
                {
                    "skuCode":"SKU001",
                    "price":"100.00",
                    "quantity":5
                }
                """;

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
