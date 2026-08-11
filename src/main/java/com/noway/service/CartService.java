package com.noway.service;

import com.noway.dto.CartResponse;
import com.noway.entity.CartItem;
import com.noway.entity.Product;
import com.noway.entity.User;
import com.noway.exception.BadRequestException;
import com.noway.exception.CartItemNotFoundException;
import com.noway.exception.InsufficientStockException;
import com.noway.exception.UnauthorizedAccessException;
import com.noway.repository.CartItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    public CartService(CartItemRepository cartItemRepository, ProductService productService) {
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }

    public List<CartItem> getCartItems(User user) {
        return cartItemRepository.findByUser(user);
    }

    public CartResponse getCartResponse(User user) {
        return CartResponse.of(getCartItems(user));
    }

    public CartItem addToCart(User user, Long productId, int quantity) {
        if (quantity < 1) {
            throw new BadRequestException("Quantity must be at least 1.");
        }
        Product product = productService.getProduct(productId);
        if (product.getStock() < quantity) {
            throw new InsufficientStockException(
                    "Only " + product.getStock() + " item(s) available for '" + product.getName() + "'.");
        }

        return cartItemRepository.findByUserAndProduct(user, product)
                .map(existing -> {
                    int newQuantity = existing.getQuantity() + quantity;
                    if (product.getStock() < newQuantity) {
                        throw new InsufficientStockException(
                                "Only " + product.getStock() + " item(s) available for '" + product.getName() + "'.");
                    }
                    existing.setQuantity(newQuantity);
                    return cartItemRepository.save(existing);
                })
                .orElseGet(() -> cartItemRepository.save(new CartItem(user, product, quantity)));
    }

    public void updateQuantity(User user, Long cartItemId, int quantity) {
        if (quantity < 1) {
            throw new BadRequestException("Quantity must be at least 1.");
        }
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found."));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You cannot modify this cart item.");
        }
        if (item.getProduct().getStock() < quantity) {
            throw new InsufficientStockException(
                    "Only " + item.getProduct().getStock() + " item(s) available for '" + item.getProduct().getName() + "'.");
        }
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    public void removeFromCart(User user, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found."));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You cannot modify this cart item.");
        }
        cartItemRepository.delete(item);
    }

    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }
}
