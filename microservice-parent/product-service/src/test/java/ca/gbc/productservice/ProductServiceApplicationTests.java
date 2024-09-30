package ca.gbc.productservice;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;


//tells spring boots to look for a main configuration class(@SpringBootApplication)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests {

    //This Annotation is used in combination with TestContainers to automatically configure the connection to the
    // Test MongoDBContainer

    @ServiceConnection
    static MongoDBContainer mongoDBContainer= new MongoDBContainer("mongo:latest");

    @LocalServerPort
    private Integer port;
    //http://localhost:port/api/ptoduct

    @BeforeEach
    void setup(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

    }
    static {
        mongoDBContainer.start();
    }

    @Test
    void createProductTest(){
        String requestBody= """
                {
                "name" : "Roku Tv",
                "description" : "The Roku Tv - Model 2004",
                "price" : "600";
                }
                """;
        //BDD -0 Behavioural Driven Development(Given, When, Then)
        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/products")
                .then()
                .log().all()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("name", Matchers.equalTo("Roku Tv"))
                .body("description", Matchers.equalTo("The Roku Tv - Model 2004"))
                .body("price", Matchers.equalTo("600"));


    }

    @Test
    void getAllProducts(){
        String requestBody= """
                {
                "name" : "Roku Tv",
                "description" : "The Roku Tv - Model 2004",
                "price" : "600";
                }
                """;
        //BDD -0 Behavioural Driven Development(Given, When, Then)
        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/products")
                .then()
                .log().all()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("name", Matchers.equalTo("Roku Tv"))
                .body("description", Matchers.equalTo("The Roku Tv - Model 2004"))
                .body("price", Matchers.equalTo("600"));

        RestAssured.given()
                .contentType("application/json")
                .when()
                .get("/api/products")
                .then()
                .log().all()
                .statusCode(200)
                .body("size()", Matchers.greaterThan(0))
                .body("[0].id", Matchers.notNullValue())
                .body("[0].name", Matchers.equalTo("Roku Tv"))
                .body("[0].description", Matchers.equalTo("The Roku Tv - Model 2004"))
                .body("[0].price", Matchers.equalTo("600"));



    }



}
