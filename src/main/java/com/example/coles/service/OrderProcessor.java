package com.example.coles.service;

import com.example.coles.domain.Order;
import org.springframework.stereotype.Service;

@Service
public class OrderProcessor {

    public void processOrder(Order order) {
        // Simulate order processing logic
        System.out.println("Processing order: " + order.getId());
    }
}
