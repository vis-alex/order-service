package com.alex.vis.orderservice.controller;

import com.alex.vis.orderservice.client.InventoryClient;
import com.alex.vis.orderservice.dto.OrderDto;
import com.alex.vis.orderservice.model.Order;
import com.alex.vis.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/order")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    @PostMapping
    public String placeOrder(@RequestBody OrderDto orderDto, @RequestHeader("Authorization") String authHeader) {

        System.out.println("In the order controller");

        System.out.println("Authorization:"  + authHeader);

        boolean isAllProductsInStock = orderDto.getOrderLineItems().stream()
                .allMatch(orderLineItem -> inventoryClient.checkStock(orderLineItem.getScuCode()));

        System.out.println("Unrecheble place");

        if (isAllProductsInStock) {
            Order order = new Order();
            order.setOrderLineItems(orderDto.getOrderLineItems());
            order.setOrderNumber(UUID.randomUUID().toString());

            orderRepository.save(order);

            return "Order place successfully";
        }

        return "Please try again";
    }
}