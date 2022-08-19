package com.alex.vis.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

//Feign client provides load balance module which good auto integrates with eureka
//Create spring bean. We can inject it
@FeignClient(name = "inventory-service")
public interface InventoryClient {
    @GetMapping("/api/inventory/{scuCode}")
    Boolean checkStock(@PathVariable String scuCode);
}