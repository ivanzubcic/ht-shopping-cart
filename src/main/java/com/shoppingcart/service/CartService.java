package com.shoppingcart.service;

import com.shoppingcart.model.Cart;
import com.shoppingcart.model.Item;
import com.shoppingcart.repository.CartRepository;
import com.shoppingcart.util.ResilienceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

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

    /**
     * Save a cart. If the cart with the given customerId already exists, it throws IllegalStateException.
     * If the items list is not null, it sets the action timestamp of any item which does not have it set to the current instant.
     *
     * @param cart the cart to save
     * @return the saved cart
     * @throws IllegalStateException if the cart with the given customerId already exists
     */
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

    public Cart addItemToCart(String customerId, Item item) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Cart not found for customerId: " + customerId));
        item.setActionTimestamp(java.time.Instant.now());
        cart.getItems().add(item);
        return cartRepository.save(cart);
    }

    /**
     * Deletes a cart by customerId with fault tolerance.
     * <p>
     * This method uses both a circuit breaker and a retry pattern with exponential backoff.
     * If a transient error occurs (e.g., database timeout), it will retry up to 3 times,
     * waiting 500ms, then 1000ms, then 2000ms between attempts. If failures persist,
     * the circuit breaker will open and further attempts will fail fast for a period of time.
     *
     * @param customerId the customer id of the cart to delete
     * @throws RuntimeException if the delete operation fails after all retries or while the circuit breaker is open
     */
    public void deleteCartByCustomerId(String customerId) {
        ResilienceUtils.runWithResilience(() -> {
            Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Cart not found for customerId: " + customerId));
            cartRepository.deleteById(cart.getId());
        });
    }

    /**
     * Statistics: count the number of items in all carts which were sold with the given offerId, action,
     * and in the given period.
     *
     * @param offerId the offer id
     * @param action  the action
     * @param from    the start of the period
     * @param to      the end of the period
     * @return the total number of items
     */
    public long countOffersSold(String offerId, String action, Instant from, Instant to) {
        return ResilienceUtils.callWithResilience(() ->
            cartRepository.findCartsByItemDynamic(offerId, action, from, to).stream()
                .flatMap(cart -> cart.getItems().stream())
                .filter(item -> item.getOfferId().equals(offerId))
                .filter(item -> item.getAction().name().equalsIgnoreCase(action))
                .filter(item -> item.getActionTimestamp().isAfter(from) && item.getActionTimestamp().isBefore(to))
                .count()
        );
    }

    /**
     * Statistics: count how many distinct customers bought offers of a particular id and action in a period.
     *
     * @param offerId the offer id
     * @param action  the action
     * @param from    the start of the period
     * @param to      the end of the period
     * @return the number of distinct customers
     */
    public long countUniqueCustomers(String offerId, String action, Instant from, Instant to) {
        return ResilienceUtils.callWithResilience(() ->
            cartRepository.findCartsByItemDynamic(offerId, action, from, to).stream()
                .flatMap(cart -> cart.getItems().stream()
                        .filter(item -> item.getOfferId().equals(offerId))
                        .filter(item -> item.getAction().name().equalsIgnoreCase(action))
                        .filter(item -> item.getActionTimestamp().isAfter(from) && item.getActionTimestamp().isBefore(to))
                        .map(item -> cart.getCustomerId()))
                .distinct()
                .count()
        );
    }

    /**
     * Statistics: count the total number of items with a specific offer id and action sold in a given period.
     *
     * @param offerId the offer id
     * @param action  the action
     * @param from    the start of the period
     * @param to      the end of the period
     * @return the total number of items
     */
    public long countTotalItems(String offerId, String action, Instant from, Instant to) {
        return ResilienceUtils.callWithResilience(() ->
            cartRepository.findCartsByItemDynamic(offerId, action, from, to).stream()
                .flatMap(cart -> cart.getItems().stream())
                .filter(item -> item.getOfferId().equals(offerId))
                .filter(item -> item.getAction().name().equalsIgnoreCase(action))
                .filter(item -> item.getActionTimestamp().isAfter(from) && item.getActionTimestamp().isBefore(to))
                .count()
        );
    }
}
