package com.noway.controller;

import com.noway.dto.ApiResponse;
import com.noway.dto.OrderResponse;
import com.noway.entity.Order;
import com.noway.exception.EmptyCartException;
import com.noway.exception.InsufficientStockException;
import com.noway.exception.UnauthorizedAccessException;
import com.noway.security.SecurityUtils;
import com.noway.service.CartService;
import com.noway.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final SecurityUtils securityUtils;

    public OrderController(OrderService orderService, CartService cartService, SecurityUtils securityUtils) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/checkout")
    public String checkoutPage(Model model) {
        var cart = cartService.getCartResponse(securityUtils.currentUser());
        if (cart.items().isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("cart", cart);
        return "checkout";
    }

    @PostMapping("/checkout")
    public String checkout(Model model) {
        try {
            Order order = orderService.placeOrder(securityUtils.currentUser());
            return "redirect:/order-success?orderId=" + order.getId();
        } catch (EmptyCartException ex) {
            return "redirect:/cart?error=empty";
        } catch (InsufficientStockException ex) {
            return "redirect:/cart?error=stock";
        }
    }

    @GetMapping("/order-success")
    public String orderSuccess(@RequestParam Long orderId, Model model) {
        model.addAttribute("order", orderService.getOrderResponse(securityUtils.currentUser(), orderId));
        return "order-success";
    }

    @GetMapping("/orders")
    public String ordersPage(Model model) {
        model.addAttribute("orders", orderService.getOrders(securityUtils.currentUser()));
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetailsPage(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("order", orderService.getOrderResponse(securityUtils.currentUser(), id));
        } catch (UnauthorizedAccessException ex) {
            return "redirect:/orders?error=not-found";
        }
        return "order-details";
    }

    @PostMapping("/api/orders")
    @ResponseBody
    public ResponseEntity<?> placeOrderApi() {
        Order order = orderService.placeOrder(securityUtils.currentUser());
        return ResponseEntity.ok(OrderResponse.from(order, orderService.getOrderItems(order)));
    }

    @GetMapping("/api/orders")
    @ResponseBody
    public List<OrderResponse> ordersApi() {
        return orderService.getOrders(securityUtils.currentUser()).stream()
                .map(order -> OrderResponse.from(order, orderService.getOrderItems(order)))
                .toList();
    }

    @GetMapping("/api/orders/{id}")
    @ResponseBody
    public OrderResponse orderApi(@PathVariable Long id) {
        return orderService.getOrderResponse(securityUtils.currentUser(), id);
    }
}
