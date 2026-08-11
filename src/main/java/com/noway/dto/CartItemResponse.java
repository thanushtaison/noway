package com.noway.dto;

import com.noway.entity.CartItem;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String name,
        BigDecimal price,
        int quantity,
        BigDecimal subtotal,
        String imageUrl,
        int stock
) {

    public static CartItemResponse from(CartItem cartItem) {
        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getProduct().getPrice(),
                cartItem.getQuantity(),
                cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())),
                cartItem.getProduct().getImageUrl(),
                cartItem.getProduct().getStock()
        );
    }
}
