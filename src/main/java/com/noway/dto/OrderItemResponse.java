package com.noway.dto;

import com.noway.entity.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String productName,
        int quantity,
        BigDecimal price,
        BigDecimal subtotal
) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getPrice(),
                item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
        );
    }
}
