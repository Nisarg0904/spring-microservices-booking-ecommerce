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
    void createProductTest() {
        String requestBody= """
                {
                "product_name" : "Roku Tv",
                "product_description" : "The Roku Tv - Model 2004",
                "product_price" : 600
                }
                """;
        // RestASSURED IS USED TO PERFORM HTTP requests and verify responses
        // This test performs a Post Request to api/product endpoint to create a new product.
        // Then it verifies that the response status is 201 (Created) and the response body contains the correct product details.
        //BDD -0 Behavioural Driven Development(Given, When, Then)
        RestAssured.given()
                .contentType("application/json") // set the content type of request to json
                .body(requestBody) // pass the rquest body ( the product data)
                .when()
                .post("/api/product") // perform the post request to the /api/product endpoint
                .then()
                .log().all() // log the response details
                .statusCode(201) // Assert that the HTTP status code is 201 Created
                .body("product_id", Matchers.notNullValue()) // Assert that the return product has a non-null ID
                .body("product_name", Matchers.equalTo("Roku Tv")) // Assert that the product's name matches
                .body("product_description", Matchers.equalTo("The Roku Tv - Model 2004")) // matches
                .body("product_price", Matchers.equalTo(600)); //  matches


    }

    @Test
    void getAllProducts(){
        String requestBody= """
                {
                "product_name" : "Roku Tv",
                "product_description" : "The Roku Tv - Model 2004",
                "product_price" : 600
                }
                """;
        // RestASSURED IS USED TO PERFORM HTTP requests and verify responses
        // This test performs a Post Request to api/product endpoint to create a new product.
        // Then it verifies that the response status is 201 (Created) and the response body contains the correct product details.
        //BDD -0 Behavioural Driven Development(Given, When, Then)
        RestAssured.given()
                .contentType("application/json") // set the content type of request to json
                .body(requestBody) // pass the rquest body ( the product data)
                .when()
                .post("/api/product") // perform the post request to the /api/product endpoint
                .then()
                .log().all() // log the response details
                .statusCode(201) // Assert that the HTTP status code is 201 Created
                .body("product_id", Matchers.notNullValue()) // Assert that the return product has a non-null ID
                .body("product_name", Matchers.equalTo("Roku Tv")) // Assert that the product's name matches
                .body("product_description", Matchers.equalTo("The Roku Tv - Model 2004")) // matches
                .body("product_price", Matchers.equalTo(600)); //  matches

        RestAssured.given()
                .contentType("application/json")
                .when()
                .get("/api/product")
                .then()
                .log().all()
                .statusCode(200)
                .body("size()", Matchers.greaterThan(0))
                .body("[0].product_id", Matchers.notNullValue())
                .body("[0].product_name", Matchers.equalTo("Roku Tv"))
                .body("[0].product_description", Matchers.equalTo("The Roku Tv - Model 2004"))
                .body("[0].product_price", Matchers.equalTo(600));



    }



}
