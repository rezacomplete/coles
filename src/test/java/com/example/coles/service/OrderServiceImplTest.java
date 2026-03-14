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

    private OrderRepository repo;
    private OrderServiceImpl service;

    @BeforeEach
    void setup() {
        repo = mock(OrderRepository.class);
        service = new OrderServiceImpl(repo);
    }

    @Test
    void create_shouldSaveAndReturn() {
        OrderRequest req = new OrderRequest();
        req.setCustomerName("Alice");
        req.setAmount(new BigDecimal("12.50"));

        // when saved, repository returns same entity; capture save
        when(repo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        var resp = service.create(req);

        assertThat(resp.getCustomerName()).isEqualTo("Alice");
        assertThat(resp.getAmount()).isEqualByComparingTo(new BigDecimal("12.50"));
        assertThat(resp.getStatus()).isEqualTo(OrderStatus.PENDING);

        verify(repo, times(1)).save(any(Order.class));
    }

    @Test
    void update_whenNotPending_throws() {
        UUID id = UUID.randomUUID();
        Order existing = Order.create("Bob", new BigDecimal("5.00"));
        existing.setStatus(OrderStatus.COMPLETED);
        when(repo.findById(id)).thenReturn(Optional.of(existing));

        OrderRequest req = new OrderRequest();
        req.setCustomerName("Bob2");
        req.setAmount(new BigDecimal("6.00"));

        assertThrows(InvalidOrderStateException.class, () -> service.update(id, req));
    }

    @Test
    void process_successCompletes() {
        UUID id = UUID.randomUUID();
        Order existing = Order.create("C", new BigDecimal("3.00"));
        when(repo.findById(id)).thenReturn(Optional.of(existing));
        when(repo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        var resp = service.process(id, new ProcessRequest());
        assertThat(resp.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void process_simulatedFailure_marksFailed() {
        UUID id = UUID.randomUUID();
        Order existing = Order.create("D", new BigDecimal("8.00"));
        when(repo.findById(id)).thenReturn(Optional.of(existing));
        when(repo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        ProcessRequest req = new ProcessRequest();
        req.setSimulateFailure(true);

        var resp = service.process(id, req);
        assertThat(resp.getStatus()).isEqualTo(OrderStatus.FAILED);
    }
}
