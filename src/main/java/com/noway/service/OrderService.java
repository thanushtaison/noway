package com.noway.service;

import com.noway.dto.OrderResponse;
import com.noway.entity.CartItem;
import com.noway.entity.Order;
import com.noway.entity.OrderItem;
import com.noway.entity.OrderStatus;
import com.noway.entity.Product;
import com.noway.entity.User;
import com.noway.exception.EmptyCartException;
import com.noway.exception.InsufficientStockException;
import com.noway.exception.UnauthorizedAccessException;
import com.noway.repository.CartItemRepository;
import com.noway.repository.OrderItemRepository;
import com.noway.repository.OrderRepository;
import com.noway.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartItemRepository cartItemRepository,
                        ProductRepository productRepository,
                        CartService cartService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    @Transactional
    public Order placeOrder(User user) {
        List<CartItem> cart = cartService.getCartItems(user);
        if (cart.isEmpty()) {
            throw new EmptyCartException("Your cart is empty. Add items before checkout.");
        }

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart) {
            Product product = cartItem.getProduct();
            int quantity = cartItem.getQuantity();
            if (product.getStock() < quantity) {
                throw new InsufficientStockException(
                        "Insufficient stock for '" + product.getName() + "'. Available: " + product.getStock());
            }
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
            orderItems.add(new OrderItem(product.getId(), product.getName(), quantity, product.getPrice()));
        }

        Order order = new Order(user, total, OrderStatus.CONFIRMED);
        order.setItems(orderItems);
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(order);
        }
        orderRepository.save(order);

        for (CartItem cartItem : cart) {
            Product product = cartItem.getProduct();
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        cartService.clearCart(user);
        return order;
    }

    public List<Order> getOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Order getOrderForUser(User user, Long orderId) {
        return orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new UnauthorizedAccessException("Order not found or you do not have access to it."));
    }

    public List<OrderItem> getOrderItems(Order order) {
        return orderItemRepository.findByOrder(order);
    }

    public OrderResponse getOrderResponse(User user, Long orderId) {
        Order order = getOrderForUser(user, orderId);
        return OrderResponse.from(order, getOrderItems(order));
    }
}
