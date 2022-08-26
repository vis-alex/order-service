package com.alex.vis.orderservice.controller;

import com.alex.vis.orderservice.client.InventoryClient;
import com.alex.vis.orderservice.dto.OrderDto;
import com.alex.vis.orderservice.model.Order;
import com.alex.vis.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreaker;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/order")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    private final Resilience4JCircuitBreakerFactory circuitBreakerFactory;

    //Stream Bridge - component which will follow messages to specific output binder
    private final StreamBridge streamBridge;

    //Need to be the same thread name - traceableExecutorService
    private final ExecutorService traceableExecutorService;
    @PostMapping
    public String placeOrder(@RequestBody OrderDto orderDto) {

        //we are creating the instance of circuit breaker object
        Resilience4JCircuitBreaker breaker = circuitBreakerFactory.create("inventory");

        //Do the supplier , which returns have the stock product or not
        Supplier<Boolean> booleanSupplier = () -> orderDto.getOrderLineItems().stream()
                .allMatch(orderLineItem -> {
                    log.info("Making call to inventory service for ScuCode {}", orderLineItem.getScuCode());
                    return inventoryClient.checkStock(orderLineItem.getScuCode());
                });

        //If service fall, then we handle exception
        boolean isAllProductsInStock = breaker.run(booleanSupplier, throwable -> handleErrorCase());

        if (isAllProductsInStock) {
            Order order = new Order();
            order.setOrderLineItems(orderDto.getOrderLineItems());
            order.setOrderNumber(UUID.randomUUID().toString());

            orderRepository.save(order);

            log.info("Sending Order Details wit Order Id {} to notification Service", order.getId());
            //I send order_id to the binding `notificationEventSupplier-out-0`
            streamBridge.send("notificationEventSupplier-out-0",
                    MessageBuilder.withPayload(order.getId()).build());

            return "Order place successfully";
        }

        return "Please try again";
    }

    //Return false, cause it`s the same when we haven`t the product in stock
    private Boolean handleErrorCase() {
        return false;
    }

//    @PostMapping
//    public String placeOrder(@RequestBody OrderDto orderDto, @RequestHeader("Authorization") String authHeader) {
//
//        System.out.println("In the order controller");
//
//        System.out.println("Authorization:"  + authHeader);
//
//        boolean isAllProductsInStock = orderDto.getOrderLineItems().stream()
//                .allMatch(orderLineItem -> inventoryClient.checkStock(orderLineItem.getScuCode()));
//
//        System.out.println("Unreachable place");
//
//        if (isAllProductsInStock) {
//            Order order = new Order();
//            order.setOrderLineItems(orderDto.getOrderLineItems());
//            order.setOrderNumber(UUID.randomUUID().toString());
//
//            orderRepository.save(order);
//
//            return "Order place successfully";
//        }
//
//        return "Please try again";
//    }
}