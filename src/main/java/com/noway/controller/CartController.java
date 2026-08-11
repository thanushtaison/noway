package com.noway.controller;

import com.noway.dto.AddToCartRequest;
import com.noway.dto.ApiResponse;
import com.noway.dto.CartResponse;
import com.noway.dto.UpdateCartRequest;
import com.noway.security.SecurityUtils;
import com.noway.service.CartService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CartController {

    private final CartService cartService;
    private final SecurityUtils securityUtils;

    public CartController(CartService cartService, SecurityUtils securityUtils) {
        this.cartService = cartService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/cart")
    public String cartPage(Model model) {
        model.addAttribute("cart", cartService.getCartResponse(securityUtils.currentUser()));
        return "cart";
    }

    @GetMapping("/api/cart")
    @ResponseBody
    public CartResponse getCartApi() {
        return cartService.getCartResponse(securityUtils.currentUser());
    }

    @PostMapping("/api/cart")
    @ResponseBody
    public ApiResponse addToCartApi(@Valid @RequestBody AddToCartRequest request) {
        cartService.addToCart(securityUtils.currentUser(), request.productId(), request.quantity());
        return ApiResponse.ok("Added to cart.");
    }

    @PutMapping("/api/cart/{id}")
    @ResponseBody
    public ApiResponse updateCartApi(@PathVariable Long id, @Valid @RequestBody UpdateCartRequest request) {
        cartService.updateQuantity(securityUtils.currentUser(), id, request.quantity());
        return ApiResponse.ok("Cart updated.");
    }

    @DeleteMapping("/api/cart/{id}")
    @ResponseBody
    public ApiResponse removeFromCartApi(@PathVariable Long id) {
        cartService.removeFromCart(securityUtils.currentUser(), id);
        return ApiResponse.ok("Removed from cart.");
    }
}
