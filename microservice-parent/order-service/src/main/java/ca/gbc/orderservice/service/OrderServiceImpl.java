package ca.gbc.orderservice.service;

import ca.gbc.orderservice.client.InventoryClient;
import ca.gbc.orderservice.dto.OrderRequest;
import ca.gbc.orderservice.event.OrderPlacedEvent;
import ca.gbc.orderservice.model.Order;
import ca.gbc.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional

public class OrderServiceImpl implements OrderService {

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private final OrderRepository orderRepository;

    private final InventoryClient inventoryClient;

    @Override
    public void placeOrder(OrderRequest orderRequest) {

        // check the inventory

        var isProductInStock = inventoryClient.isInStock(orderRequest.skuCode(),orderRequest.quantity());

        if(isProductInStock) {
            Order order = Order.builder()
                    .orderNumber(UUID.randomUUID().toString())
                    .price(orderRequest.price())
                    .skuCode(orderRequest.skuCode())
                    .quantity(orderRequest.quantity())
                    .build();
            orderRepository.save(order);

            OrderPlacedEvent orderPlacedEvent=
                    new OrderPlacedEvent(order.getOrderNumber(),orderRequest.userDetails().email());
            log.info("Start- sending orderPlaced {} to kafka topic order--placed ", orderPlacedEvent);
            kafkaTemplate.send("order-placed", orderPlacedEvent);
            log.info("Complete- sending orderPlaced {} to kafka topic order--placed ", orderPlacedEvent);

        }
        else {
            throw new RuntimeException("Order not in stock");
        }


    }
}
