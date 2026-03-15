package com.example.coles.controller;

import com.example.coles.api.OrderRequest;
import com.example.coles.api.OrderResponse;
import com.example.coles.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public OrderResponse create(@RequestBody OrderRequest request) {
        validate(request);
        return service.create(request);
    }

    @GetMapping
    public List<OrderResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping("/{id}/process")
    public OrderResponse process(@PathVariable UUID id) {
        return service.process(id);
    }

    private void validate(OrderRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request body is required");
        }

        String name = request.getCustomerName();
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerName must not be blank");
        }

        BigDecimal amount = request.getAmount();
        if (amount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amount is required");
        }

        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amount must be non-zero");
        }
    }
}
