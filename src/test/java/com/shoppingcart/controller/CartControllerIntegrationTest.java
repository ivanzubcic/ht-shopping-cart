package com.shoppingcart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingcart.ShoppingCartApplication;
import com.shoppingcart.model.Cart;
import com.shoppingcart.model.Item;
import com.shoppingcart.model.Price;
import com.shoppingcart.repository.CartRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ShoppingCartApplication.class)
@AutoConfigureMockMvc
public class CartControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartRepository cartRepository;

    private final Set<String> testCustomerIds = new HashSet<>();

    @AfterEach
    void cleanUp() {
        // Remove only carts inserted by this test class
        for (String customerId : testCustomerIds) {
            cartRepository.findByCustomerId(customerId).ifPresent(cart -> cartRepository.deleteById(cart.getId()));
        }
        testCustomerIds.clear();
    }

    @Test
    void testCreateAndGetCart() throws Exception {
        Cart cart = new Cart();
        cart.setCustomerId("test-customer");
        testCustomerIds.add("test-customer");
        Item item = new Item();
        item.setOfferId("tv-001");
        item.setAction(Item.Action.ADD);
        item.setActionTimestamp(Instant.now());
        Price price = new Price();
        price.setType(Price.PriceType.RECURRING);
        price.setValue(new BigDecimal("19.99"));
        price.setRecurrences(12);
        item.setPrices(List.of(price));
        cart.setItems(List.of(item));

        // POST cart
        mockMvc.perform(post("/api/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cart)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("test-customer"));

        // GET cart
        mockMvc.perform(get("/api/carts/test-customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("test-customer"));
    }

    @Test
    void testDeleteCart() throws Exception {
        // Create cart first
        Cart cart = new Cart();
        cart.setCustomerId("delete-customer");
        testCustomerIds.add("delete-customer");
        Item item = new Item();
        item.setOfferId("tv-002");
        item.setAction(Item.Action.ADD);
        item.setActionTimestamp(Instant.now());
        Price price = new Price();
        price.setType(Price.PriceType.ONE_TIME);
        price.setValue(new BigDecimal("9.99"));
        item.setPrices(List.of(price));
        cart.setItems(List.of(item));
        mockMvc.perform(post("/api/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cart)))
                .andExpect(status().isOk());

        // DELETE cart
        mockMvc.perform(delete("/api/carts/delete-customer"))
                .andExpect(status().isNoContent());

        // GET should return 404
        mockMvc.perform(get("/api/carts/delete-customer"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddItemToCart() throws Exception {
        // First, create a cart with one item
        Cart cart = new Cart();
        cart.setCustomerId("put-test-user");
        testCustomerIds.add("put-test-user");
        Item item = new Item();
        item.setOfferId("offer-put-1");
        item.setAction(Item.Action.ADD);
        item.setActionTimestamp(Instant.now());
        Price price = new Price();
        price.setType(Price.PriceType.ONE_TIME);
        price.setValue(new BigDecimal("49.99"));
        item.setPrices(List.of(price));
        cart.setItems(List.of(item));

        mockMvc.perform(post("/api/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cart)))
                .andExpect(status().isOk());

        // Now, add a new item to the existing cart via PUT
        Item newItem = new Item();
        newItem.setOfferId("offer-put-2");
        newItem.setAction(Item.Action.ADD);
        newItem.setActionTimestamp(Instant.now());
        Price newPrice = new Price();
        newPrice.setType(Price.PriceType.RECURRING);
        newPrice.setValue(new BigDecimal("9.99"));
        newPrice.setRecurrences(6);
        newItem.setPrices(List.of(newPrice));

        mockMvc.perform(put("/api/carts/put-test-user/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("put-test-user"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2));
    }
}
