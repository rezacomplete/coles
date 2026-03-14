package com.example.coles.service;

import com.example.coles.api.OrderRequest;
import com.example.coles.api.ProcessRequest;
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

    @BeforeEach
    void setup() {
        orderRepository = mock(OrderRepository.class);
        orderService = new OrderServiceImpl(orderRepository);
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
    void update_whenNotPending_throws() {
        UUID id = UUID.randomUUID();
        Order existing = Order.create("Bob", new BigDecimal("5.00"));
        existing.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(id)).thenReturn(Optional.of(existing));

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCustomerName("Bob2");
        orderRequest.setAmount(new BigDecimal("6.00"));

        assertThrows(InvalidOrderStateException.class, () -> orderService.update(id, orderRequest));
    }

    @Test
    void update_whenOrderNotFound_throws() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCustomerName("Nobody");
        orderRequest.setAmount(new BigDecimal("1.00"));

        assertThrows(OrderNotFoundException.class, () -> orderService.update(id, orderRequest));
    }

    @Test
    void update_whenUpdatesAndSavesSuccessfully() {
        UUID id = UUID.randomUUID();
        Order existing = Order.create("Original", new BigDecimal("10.00"));
        // ensure it's pending so update is allowed
        existing.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(Order.class))).thenReturn(existing);

        OrderRequest update = new OrderRequest();
        update.setCustomerName("Updated Name");
        update.setAmount(new BigDecimal("11.50"));

        var result = orderService.update(id, update);

        assertThat(result.getCustomerName()).isEqualTo("Updated Name");
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("11.50"));
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void process_whenOrderNotFound_throws() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.process(id, new ProcessRequest()));
    }

    @Test
    void process_whenStatusNotPending_throws() {
        UUID id = UUID.randomUUID();
        Order existing = Order.create("E", new BigDecimal("4.00"));
        existing.setStatus(OrderStatus.PROCESSING);
        when(orderRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(InvalidOrderStateException.class, () -> orderService.process(id, new ProcessRequest()));
    }

    @Test
    void process_whenSuccessful_savesProcessingThenCompleted() {
        UUID id = UUID.randomUUID();
        Order existing = Order.create("Success", new BigDecimal("7.00"));
        existing.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(id)).thenReturn(Optional.of(existing));

        var result = orderService.process(id, new ProcessRequest());

        assertThat(result.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void delete_whenOrderNotFound_throws() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.delete(id));
        assertThrows(OrderNotFoundException.class, () -> orderService.delete(id));
    }

    //todo add test for delete when status processing or completed

    //todo add test for delete when successful

    @Test
    void process_successCompletes() {
        UUID id = UUID.randomUUID();
        Order existing = Order.create("C", new BigDecimal("3.00"));
        when(orderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(Order.class))).thenReturn(existing);

        var result = orderService.process(id, new ProcessRequest());
        assertThat(result.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void process_simulatedFailure_marksFailed() {
        UUID id = UUID.randomUUID();
        Order existing = Order.create("D", new BigDecimal("8.00"));
        when(orderRepository.findById(id)).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(Order.class))).thenReturn(existing);

        ProcessRequest request = new ProcessRequest();
        request.setSimulateFailure(true);

        var resp = orderService.process(id, request);
        assertThat(resp.getStatus()).isEqualTo(OrderStatus.FAILED);
    }
}
