package com.example.coles.service;

import com.example.coles.api.OrderRequest;
import com.example.coles.api.OrderResponse;
import com.example.coles.api.ProcessRequest;
import com.example.coles.domain.Order;
import com.example.coles.domain.OrderStatus;
import com.example.coles.repo.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;

    public OrderServiceImpl(OrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrderResponse create(OrderRequest request) {
        Order order = Order.create(request.getCustomerName(), request.getAmount());
        repository.save(order);
        return toResponse(order);
    }

    @Override
    public OrderResponse get(UUID id) {
        Order order = repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        return toResponse(order);
    }

    @Override
    public List<OrderResponse> list() {
        return repository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public OrderResponse update(UUID id, OrderRequest request) {
        Order order = repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Only pending orders can be updated");
        }

        order.setCustomerName(request.getCustomerName());
        order.setAmount(request.getAmount());
        order.setUpdatedAt(LocalDateTime.now());
        repository.save(order);
        return toResponse(order);
    }

    @Override
    public void delete(UUID id) {
        Order order = repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() == OrderStatus.PROCESSING || order.getStatus() == OrderStatus.COMPLETED) {
            throw new InvalidOrderStateException("Cannot delete processing or completed orders");
        }

        repository.delete(order);
    }

    @Override
    public OrderResponse process(UUID id, ProcessRequest request) {
        Order order = repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Only pending orders can be processed");

        }

        order.setStatus(OrderStatus.PROCESSING);
        order.setUpdatedAt(LocalDateTime.now());
        repository.save(order);

        try {
            // Simulate processing work
            if (request != null && request.isSimulateFailure()) {
                throw new RuntimeException("Simulated failure");
            }

            // processing succeeded
            order.setStatus(OrderStatus.COMPLETED);
            order.setUpdatedAt(LocalDateTime.now());
            repository.save(order);

        } catch (Exception ex) {
            order.setStatus(OrderStatus.FAILED);
            order.setUpdatedAt(LocalDateTime.now());
            repository.save(order);
        }

        return toResponse(order);
    }

    private OrderResponse toResponse(Order o) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setId(o.getId());
        orderResponse.setCustomerName(o.getCustomerName());
        orderResponse.setAmount(o.getAmount());
        orderResponse.setStatus(o.getStatus());
        orderResponse.setCreatedAt(o.getCreatedAt());
        orderResponse.setUpdatedAt(o.getUpdatedAt());
        return orderResponse;
    }
}
