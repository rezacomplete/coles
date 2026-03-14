package com.example.coles.service;

import com.example.coles.api.OrderRequest;
import com.example.coles.api.OrderResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderResponse create(OrderRequest request);

    OrderResponse get(UUID id);

    List<OrderResponse> list();

    OrderResponse process(UUID id);
}
