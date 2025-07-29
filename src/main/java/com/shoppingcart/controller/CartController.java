package com.shoppingcart.controller;

import com.shoppingcart.dto.StatisticsResponse;
import com.shoppingcart.model.Cart;
import com.shoppingcart.model.Item;
import com.shoppingcart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<Cart> getCart(@PathVariable String customerId) {
        Cart cart = cartService.getCartByCustomerId(customerId)
            .orElseThrow(() -> new java.util.NoSuchElementException("Cart not found for customerId: " + customerId));
        return ResponseEntity.ok(cart);
    }

    @PostMapping
    public ResponseEntity<Cart> saveCart(@Valid @RequestBody Cart cart) {
        Cart saved = cartService.saveCart(cart);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCart(@PathVariable String customerId) {
        cartService.deleteCartByCustomerId(customerId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{customerId}/items")
    public ResponseEntity<Cart> addItemToCart(@PathVariable String customerId, @RequestBody Item item) {
        Cart updatedCart = cartService.addItemToCart(customerId, item);
        return ResponseEntity.ok(updatedCart);
    }

    // Statistics endpoint
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> getStatistics(
            @RequestParam String offerId,
            @RequestParam String action,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        Instant fromTime = from != null ? from : Instant.EPOCH;
        Instant toTime = to != null ? to : Instant.now();
        long count = cartService.countOffersSold(offerId, action, fromTime, toTime);
        long uniqueCustomers = cartService.countUniqueCustomers(offerId, action, fromTime, toTime);
        long totalItems = cartService.countTotalItems(offerId, action, fromTime, toTime);
        StatisticsResponse response = new StatisticsResponse(
            offerId, action, fromTime, toTime, count, uniqueCustomers, totalItems
        );
        return ResponseEntity.ok(response);
    }
}
