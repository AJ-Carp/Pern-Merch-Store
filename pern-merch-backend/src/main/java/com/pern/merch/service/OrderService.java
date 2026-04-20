package com.pern.merch.service;

import com.pern.merch.dto.OrderDTO;
import com.pern.merch.dto.OrderItemDTO;
import com.pern.merch.entity.*;
import com.pern.merch.repository.CartItemRepository;
import com.pern.merch.repository.OrderRepository;
import com.pern.merch.repository.ProductRepository;
import com.pern.merch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderDTO checkout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        BigDecimal total = BigDecimal.ZERO;
        Order order = Order.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.CONFIRMED)
                .totalAmount(BigDecimal.ZERO)
                .build();

        for (CartItem ci : cartItems) {
            /* Re-fetch the product with a pessimistic lock so no other transaction
               can modify its stock until this checkout transaction completes. */
            Product product = productRepository.findByIdForUpdate(ci.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product no longer exists"));

            if (product.getStockQuantity() < ci.getQuantity()) {
                throw new RuntimeException("Not enough stock for: " + product.getName());
            }

            // Decrement stock — JPA dirty checking will flush this update when the transaction commits.
            product.setStockQuantity(product.getStockQuantity() - ci.getQuantity());

            /* If we fetch an existing entity inside a @Transactional method and 
               modify any of its fields, JPA automatically issues an UPDATE when the transaction commits (method ends). */
            // so we dont need to save product
            
            BigDecimal lineTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(ci.getQuantity()));
            total = total.add(lineTotal);

            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(ci.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();
            order.getItems().add(oi);
        }

        order.setTotalAmount(total);
        orderRepository.save(order);

        // Clear the cart
        cartItemRepository.deleteByUser(user);

        return toDTO(order);
    }

    public List<OrderDTO> getOrderHistory(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return orderRepository.findByUserOrderByOrderDateDesc(user)
                .stream().map(this::toDTO).toList();
    }

    private OrderDTO toDTO(Order order) {
        List<OrderItemDTO> items = order.getItems().stream()
                .map(oi -> OrderItemDTO.builder()
                        .productId(oi.getProduct().getId())
                        .productName(oi.getProduct().getName())
                        .quantity(oi.getQuantity())
                        .priceAtPurchase(oi.getPriceAtPurchase())
                        .build())
                .toList();

        return OrderDTO.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .items(items)
                .build();
    }
}
