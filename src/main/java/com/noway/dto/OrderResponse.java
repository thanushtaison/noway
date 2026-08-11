package com.noway.dto;

import com.noway.entity.Order;
import com.noway.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        BigDecimal totalAmount,
        String status,
        LocalDateTime createdAt,
        String userEmail,
        List<OrderItemResponse> items
) {

    public static OrderResponse from(Order order, List<OrderItem> items) {
        return new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getCreatedAt(),
                order.getUser().getEmail(),
                items.stream().map(OrderItemResponse::from).toList()
        );
    }
}
