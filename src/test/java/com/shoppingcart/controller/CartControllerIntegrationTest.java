package com.shoppingcart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingcart.ShoppingCartApplication;
import com.shoppingcart.model.Cart;
import com.shoppingcart.model.Item;
import com.shoppingcart.model.Price;
import com.shoppingcart.repository.CartRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ShoppingCartApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class CartControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartRepository cartRepository;

    private final Set<String> testCustomerIds = new HashSet<>();

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.5");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        Cart cart = new Cart();
        cart.setCustomerId("test-user");
        cart.setItems(List.of()); // Add items as needed for your test
        cartRepository.save(cart);
    }

    @AfterEach
    void cleanUp() {
        // DELETE only carts inserted by this test class
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

    @Test
    void testStatisticsEndpoint() throws Exception {
        // Insert carts with different offerId/action combinations
        Cart cart1 = new Cart();
        cart1.setCustomerId("stats-user-1");
        Item item1 = new Item();
        item1.setOfferId("OFFER1");
        item1.setAction(Item.Action.ADD);
        item1.setActionTimestamp(Instant.now().minusSeconds(3600));
        cart1.setItems(List.of(item1));
        cartRepository.save(cart1);

        Cart cart2 = new Cart();
        cart2.setCustomerId("stats-user-2");
        Item item2 = new Item();
        item2.setOfferId("OFFER1");
        item2.setAction(Item.Action.ADD);
        item2.setActionTimestamp(Instant.now().minusSeconds(1800));
        cart2.setItems(List.of(item2));
        cartRepository.save(cart2);

        Cart cart3 = new Cart();
        cart3.setCustomerId("stats-user-3");
        Item item3 = new Item();
        item3.setOfferId("OFFER2");
        item3.setAction(Item.Action.DELETE);
        item3.setActionTimestamp(Instant.now());
        cart3.setItems(List.of(item3));
        cartRepository.save(cart3);

        // Test statistics for OFFER1/ADD
        mockMvc.perform(get("/api/carts/statistics")
                .param("offerId", "OFFER1")
                .param("action", "ADD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offerId").value("OFFER1"))
                .andExpect(jsonPath("$.action").value("ADD"))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.uniqueCustomers").value(2))
                .andExpect(jsonPath("$.totalItems").value(2));

        // Test statistics for OFFER2/DELETE
        mockMvc.perform(get("/api/carts/statistics")
                .param("offerId", "OFFER2")
                .param("action", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offerId").value("OFFER2"))
                .andExpect(jsonPath("$.action").value("DELETE"))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.uniqueCustomers").value(1))
                .andExpect(jsonPath("$.totalItems").value(1));
    }
}
