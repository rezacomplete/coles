package com.example.coles.controller;

import com.example.coles.api.OrderRequest;
import com.example.coles.api.OrderResponse;
import com.example.coles.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderControllerTest {

    private OrderService service;
    private OrderController controller;

    @BeforeEach
    void setUp() {
        service = mock(OrderService.class);
        controller = new OrderController(service);
    }

    @Test
    void create_nullRequest_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> controller.create(null));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).isEqualTo("request body is required");
    }

    @Test
    void create_blankCustomerName_throwsBadRequest() {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("   ");
        request.setAmount(new BigDecimal("1.00"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> controller.create(request));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).isEqualTo("customerName must not be blank");
    }

    @Test
    void create_amountNull_throwsBadRequest() {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("Alice");
        request.setAmount(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> controller.create(request));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).isEqualTo("amount is required");
    }

    @Test
    void create_amountZero_throwsBadRequest() {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("Bob");
        request.setAmount(BigDecimal.ZERO);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> controller.create(request));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).isEqualTo("amount must be non-zero");
    }

    @Test
    void create_validRequest_delegatesToServiceAndReturnsCreated() {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("Valid");
        request.setAmount(new BigDecimal("5.00"));

        OrderResponse response = new OrderResponse();
        response.setId(UUID.randomUUID());
        response.setCustomerName("Valid");
        response.setAmount(new BigDecimal("5.00"));

        when(service.create(request)).thenReturn(response);

        var result = controller.create(request);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

}
