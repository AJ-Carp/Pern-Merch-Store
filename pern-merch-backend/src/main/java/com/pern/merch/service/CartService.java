package com.pern.merch.service;

import com.pern.merch.dto.CartItemDTO;
import com.pern.merch.entity.CartItem;
import com.pern.merch.entity.Product;
import com.pern.merch.entity.User;
import com.pern.merch.repository.CartItemRepository;
import com.pern.merch.repository.ProductRepository;
import com.pern.merch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<CartItemDTO> getCart(String username) {
        User user = findUser(username);
        return cartItemRepository.findByUser(user).stream().map(this::toDTO).toList();
    }

    public CartItemDTO addToCart(String username, Long productId, int quantity) {
        User user = findUser(username);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem cartItem = cartItemRepository.findByUserAndProductId(user, productId)
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + quantity);
                    return existing;
                })
                .orElse(CartItem.builder()
                        .user(user)
                        .product(product)
                        .quantity(quantity)
                        .build());

        return toDTO(cartItemRepository.save(cartItem));
    }

    public CartItemDTO updateQuantity(String username, Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            return null;
        }

        cartItem.setQuantity(quantity);
        return toDTO(cartItemRepository.save(cartItem));
    }

    public void removeFromCart(String username, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized");
        }

        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart(String username) {
        User user = findUser(username);
        cartItemRepository.deleteByUser(user);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private CartItemDTO toDTO(CartItem ci) {
        return CartItemDTO.builder()
                .id(ci.getId())
                .productId(ci.getProduct().getId())
                .productName(ci.getProduct().getName())
                .productImageUrl(ci.getProduct().getImageUrl())
                .productPrice(ci.getProduct().getPrice())
                .size(ci.getProduct().getSize())
                .quantity(ci.getQuantity())
                .stockQuantity(ci.getProduct().getStockQuantity())
                .build();
    }
}
