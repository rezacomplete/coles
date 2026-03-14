package com.example.coles.controller;

import com.example.coles.api.OrderRequest;
import com.example.coles.api.OrderResponse;
import com.example.coles.api.ProcessRequest;
import com.example.coles.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<OrderResponse> create(@RequestBody OrderRequest request) {
        OrderResponse r = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(r);
    }

    @GetMapping
    public List<OrderResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public OrderResponse update(@PathVariable UUID id, @RequestBody OrderRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @PostMapping("/{id}/process")
    public OrderResponse process(@PathVariable UUID id, @RequestBody(required = false) ProcessRequest request) {
        return service.process(id, request);
    }
}
