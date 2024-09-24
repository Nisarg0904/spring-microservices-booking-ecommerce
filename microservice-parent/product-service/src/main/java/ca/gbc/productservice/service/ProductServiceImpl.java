package ca.gbc.productservice.service;

import ca.gbc.productservice.dto.ProductRequest;
import ca.gbc.productservice.dto.ProductResponse;
import ca.gbc.productservice.model.Product;
import ca.gbc.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;
    @Override
    public ProductResponse createProduct(ProductRequest productRequest) {
        log.debug("Creating a new Product {}", productRequest.product_name());
        Product product = Product.builder()
                .product_name(productRequest.product_name())
                .product_description(productRequest.product_description())
                .product_price(productRequest.product_price())
                .product_description(productRequest.product_description())
                .build();

        productRepository.save(product);
        log.info("Product {} created",product.getProduct_id());

        return new ProductResponse(product.getProduct_id(), product.getProduct_name()
                , product.getProduct_description(), product.getProduct_price());

    }

    @Override
    public List<ProductResponse> getAllProducts() {
        log.debug("Getting all products");
        List<Product> products= productRepository.findAll();
        //        return products.stream().map(product -> mapToProductResponse(product)).toList();

        return products.stream().map(this::mapToProductResponse).toList();
    }
    private ProductResponse mapToProductResponse(Product product) {
        return new ProductResponse(product.getProduct_id(),product.getProduct_name()
                ,product.getProduct_description(),product.getProduct_price());
    }

    @Override
    public String updateProduct(String productId, ProductRequest productRequest) {
        log.debug("Updating product {}", productId);

        Query query= new Query();
        query.addCriteria(Criteria.where("product_id").is(productId));
        Product product=mongoTemplate.findOne(query, Product.class);
        if(product!=null){
            product.setProduct_name(productRequest.product_name());
            product.setProduct_description(productRequest.product_description());
            product.setProduct_price(productRequest.product_price());
            return productRepository.save(product).getProduct_id();
        }
        return productId;
    }

    @Override
    public void deleteProduct(String productId) {
        log.debug("Deleting product {}", productId);
        productRepository.deleteById(productId);

    }
}
