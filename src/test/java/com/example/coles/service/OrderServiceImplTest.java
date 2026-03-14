package com.example.coles.service;

import com.example.coles.api.OrderRequest;
import com.example.coles.domain.Order;
import com.example.coles.domain.OrderStatus;
import com.example.coles.repo.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    private OrderRepository orderRepository;
    private OrderServiceImpl orderService;
    private OrderProcessor orderProcessor;

    @BeforeEach
    void setup() {
        orderRepository = mock(OrderRepository.class);
        orderProcessor = mock(OrderProcessor.class);
        orderService = new OrderServiceImpl(orderRepository,  orderProcessor);
    }

    @Test
    void create_shouldSaveAndReturn() {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("Alice");
        request.setAmount(new BigDecimal("12.50"));

        Order order = Order.create(request.getCustomerName(), request.getAmount());

        when(orderRepository.save(any(Order.class))).thenReturn(order);

        var result = orderService.create(request);

        assertThat(result.getCustomerName()).isEqualTo("Alice");
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("12.50"));
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void process_whenOrderNotFound_throws() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.process(id));
    }

    @Test
    void process_whenStatusNotPending_throws() {
        UUID id = UUID.randomUUID();
        Order existing = Order.create("E", new BigDecimal("4.00"));
        existing.setStatus(OrderStatus.PROCESSING);
        when(orderRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(InvalidOrderStateException.class, () -> orderService.process(id));
    }

    @Test
    void process_whenSuccessful_savesProcessingThenCompleted() {
        UUID id = UUID.randomUUID();
        Order existing = Order.create("Success", new BigDecimal("7.00"));
        existing.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(id)).thenReturn(Optional.of(existing));

        var result = orderService.process(id);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void process_successCompletes() {
        UUID id = UUID.randomUUID();
        Order existing = Order.create("C", new BigDecimal("3.00"));
        when(orderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(Order.class))).thenReturn(existing);

        var result = orderService.process(id);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void process_simulatedFailure_marksFailed() {
        UUID id = UUID.randomUUID();
        Order existing = Order.create("D", new BigDecimal("8.00"));
        when(orderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(Order.class))).thenReturn(existing);
        doThrow(new RuntimeException("Simulated failure")).when(orderProcessor).processOrder(any(Order.class));

        var resp = orderService.process(id);
        assertThat(resp.getStatus()).isEqualTo(OrderStatus.FAILED);
    }
}
