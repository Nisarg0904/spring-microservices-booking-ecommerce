/// hello

package ca.gbc.productservice.controller;

import ca.gbc.productservice.dto.ProductRequest;
import ca.gbc.productservice.dto.ProductResponse;
import ca.gbc.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
// Lombok annotation that generates a constructor with required field (final field) injected via constructor injection.
@RestController // Marks this class as a REST controller, allowing it to handle HTTP requests and respond with JSON OR XML.
@RequestMapping("/api/product") // Base URL for all endpoints in this controller. Request to "api/product" will be mapped here.
public class ProductController {

    private final ProductService productService;

    @PostMapping // Maps HTTP POST requests to this method. Typically used for creating new resources.
    @ResponseStatus(HttpStatus.CREATED) // sets the responses status to 201 Created when product is successfully created.
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest productRequest) {
        //  The @RequestBody annotation indicates that the request body contains a productRequest object.
        ProductResponse createdProduct =  productService.createProduct(productRequest);
        // set the headers (e.g., Location Header If you want to indicate the URL of the created resource)
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location","/api/product/"+createdProduct.product_id());
        //Return the Response Entity with the 201 created status , response body , and headers
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createdProduct);
    }

    @GetMapping //Maps HTTP GET requests to this method. Typically used to retrieve the resources.
    @ResponseStatus(HttpStatus.OK) // Sets the response status to 200 OK when the products are successfully retrieved.
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    //https://localhost:8083/api/product/jhshgfysrhb
    //@ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{productId}") // Maps HTTP put requests to this method. The {productId} is a path variable used to identify which product to update.
    // ResponseEntity<?> is used when you want to return an HTTP response without specifying thr type of the response body, meaning that the response may not contain any
    public ResponseEntity<?> updateProduct(@PathVariable("productId") String productId
            , @RequestBody ProductRequest productRequest) {
        String updatedProductId=productService.updateProduct(productId, productRequest);
        //set location header attribute
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/product/" + updatedProductId);
        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{productId}")
    //@ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> deleteProduct(@PathVariable("productId") String productId) {
        productService.deleteProduct(productId);
        return new ResponseEntity<>(    HttpStatus.NO_CONTENT);

    }

}
