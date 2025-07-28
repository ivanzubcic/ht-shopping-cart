package com.shoppingcart.service;

import com.shoppingcart.model.Cart;
import com.shoppingcart.model.Item;
import com.shoppingcart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.time.Instant;

@Service
public class CartService {
    private final CartRepository cartRepository;

    @Autowired
    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Optional<Cart> getCartByCustomerId(String customerId) {
        return cartRepository.findByCustomerId(customerId);
    }

    public Cart saveCart(Cart cart) {
        Optional<Cart> existing = cartRepository.findByCustomerId(cart.getCustomerId());
        if (existing.isPresent()) {
            throw new IllegalStateException("Cart with customerId " + cart.getCustomerId() + " already exists.");
        }
        if (cart.getItems() != null) {
            cart.getItems().stream()
                .filter(item -> item.getActionTimestamp() == null)
                .forEach(item -> item.setActionTimestamp(java.time.Instant.now()));
        }
        return cartRepository.save(cart);
    }

    public void deleteCartByCustomerId(String customerId) {
        cartRepository.findByCustomerId(customerId)
                .ifPresent(cart -> cartRepository.deleteById(cart.getId()));
    }

    // Statistics: count how many offers of a particular id and action were sold in a period
    public long countOffersSold(String offerId, String action, Instant from, Instant to) {
        return cartRepository.findAll().stream()
            .flatMap(cart -> cart.getItems().stream())
            .filter(item -> item.getOfferId().equals(offerId))
            .filter(item -> item.getAction().name().equalsIgnoreCase(action))
            .filter(item -> item.getActionTimestamp().isAfter(from) && item.getActionTimestamp().isBefore(to))
            .count();
    }

    public long countUniqueCustomers(String offerId, String action, Instant from, Instant to) {
        return cartRepository.findAll().stream()
            .flatMap(cart -> cart.getItems().stream()
                .filter(item -> item.getOfferId().equals(offerId))
                .filter(item -> item.getAction().name().equalsIgnoreCase(action))
                .filter(item -> item.getActionTimestamp().isAfter(from) && item.getActionTimestamp().isBefore(to))
                .map(item -> cart.getCustomerId()))
            .distinct()
            .count();
    }

    public long countTotalItems(String offerId, String action, Instant from, Instant to) {
        return cartRepository.findAll().stream()
            .flatMap(cart -> cart.getItems().stream())
            .filter(item -> item.getOfferId().equals(offerId))
            .filter(item -> item.getAction().name().equalsIgnoreCase(action))
            .filter(item -> item.getActionTimestamp().isAfter(from) && item.getActionTimestamp().isBefore(to))
            .count();
    }

    public Cart addItemToCart(String customerId, Item item) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Cart not found for customerId: " + customerId));
        item.setActionTimestamp(java.time.Instant.now());
        cart.getItems().add(item);
        return cartRepository.save(cart);
    }
}
