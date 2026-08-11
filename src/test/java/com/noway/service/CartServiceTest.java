package com.noway.service;

import com.noway.entity.CartItem;
import com.noway.entity.Product;
import com.noway.entity.Role;
import com.noway.entity.User;
import com.noway.exception.InsufficientStockException;
import com.noway.repository.CartItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CartService cartService;

    private User buyer() {
        return new User("Buyer", "buyer@noway.com", "password123", Role.BUYER);
    }

    private Product laptop(int stock) {
        return new Product("Laptop", "desc", new BigDecimal("45000"), "Electronics", "/images/laptop.svg", stock, null);
    }

    @Test
    void addToCart_newItem_saves() {
        User user = buyer();
        Product product = laptop(10);
        when(productService.getProduct(1L)).thenReturn(product);
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartItem item = cartService.addToCart(user, 1L, 2);

        assertEquals(2, item.getQuantity());
    }

    @Test
    void addToCart_duplicateItem_incrementsQuantity() {
        User user = buyer();
        Product product = laptop(10);
        CartItem existing = new CartItem(user, product, 2);
        when(productService.getProduct(1L)).thenReturn(product);
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(existing)).thenReturn(existing);

        cartService.addToCart(user, 1L, 3);

        assertEquals(5, existing.getQuantity());
    }

    @Test
    void addToCart_quantityExceedsStock_throwsInsufficientStock() {
        User user = buyer();
        Product product = laptop(3);
        when(productService.getProduct(1L)).thenReturn(product);

        assertThrows(InsufficientStockException.class, () -> cartService.addToCart(user, 1L, 5));
    }

    @Test
    void addToCart_duplicatePushesOverStock_throwsInsufficientStock() {
        User user = buyer();
        Product product = laptop(4);
        CartItem existing = new CartItem(user, product, 3);
        when(productService.getProduct(1L)).thenReturn(product);
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(Optional.of(existing));

        assertThrows(InsufficientStockException.class, () -> cartService.addToCart(user, 1L, 2));
    }
}
