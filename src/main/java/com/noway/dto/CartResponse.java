package com.noway.dto;

import com.noway.entity.CartItem;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        List<CartItemResponse> items,
        BigDecimal totalAmount,
        int totalItems
) {

    public static CartResponse of(List<CartItem> cartItems) {
        List<CartItemResponse> items = cartItems.stream()
                .map(CartItemResponse::from)
                .toList();
        BigDecimal total = items.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalItems = items.stream().mapToInt(CartItemResponse::quantity).sum();
        return new CartResponse(items, total, totalItems);
    }
}
