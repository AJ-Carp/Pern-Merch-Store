package com.pern.merch.controller;

import com.pern.merch.dto.AddToCartRequest;
import com.pern.merch.dto.CartItemDTO;
import com.pern.merch.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // @AuthenticationPrincipal automatically injects the details of the currently logged-in user
    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.getCart(userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<CartItemDTO> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(
                cartService.addToCart(userDetails.getUsername(), request.getProductId(), request.getQuantity()));
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartItemDTO> updateQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId,
            @RequestBody Map<String, Integer> body) {
        CartItemDTO updated = cartService.updateQuantity(
                userDetails.getUsername(), cartItemId, body.get("quantity"));
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId) {
        cartService.removeFromCart(userDetails.getUsername(), cartItemId);
        return ResponseEntity.noContent().build();
    }
}
