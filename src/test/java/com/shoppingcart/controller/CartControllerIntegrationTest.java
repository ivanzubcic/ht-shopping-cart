package com.shoppingcart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingcart.ShoppingCartApplication;
import com.shoppingcart.model.Cart;
import com.shoppingcart.model.Item;
import com.shoppingcart.model.Price;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ShoppingCartApplication.class)
@AutoConfigureMockMvc
public class CartControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateAndGetCart() throws Exception {
        Cart cart = new Cart();
        cart.setCustomerId("test-customer");
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
    void generateRandomCartsForStatistics() throws Exception {
        Random random = new Random();
        String[] offerIds = {"OFFER1", "OFFER2", "OFFER3", "OFFER4", "OFFER5"};
        Item.Action[] actions = {Item.Action.ADD, Item.Action.ADD, Item.Action.ADD, Item.Action.MODIFY, Item.Action.DELETE};
        Price.PriceType[] priceTypes = {Price.PriceType.RECURRING, Price.PriceType.ONE_TIME};
        List<String> cartIds = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            String customerId = "user" + i;
            List<Item> items = new ArrayList<>();
            int itemCount = 1 + random.nextInt(3); // 1-3 items per cart
            for (int j = 0; j < itemCount; j++) {
                Item item = new Item();
                item.setOfferId(offerIds[random.nextInt(offerIds.length)]);
                item.setAction(actions[random.nextInt(actions.length)]);
                Price price = new Price();
                price.setType(priceTypes[random.nextInt(priceTypes.length)]);
                price.setValue(BigDecimal.valueOf(10 + random.nextInt(90)));
                if (price.getType() == Price.PriceType.RECURRING) {
                    price.setRecurrences(1 + random.nextInt(12));
                }
                item.setPrices(Collections.singletonList(price));
                // Set a random actionTimestamp within the last 7 days
                Instant randomTimestamp = Instant.now().minusSeconds(new Random().nextInt(7 * 24 * 3600));
                item.setActionTimestamp(randomTimestamp);
                items.add(item);
            }
            Cart cart = new Cart();
            cart.setCustomerId(customerId);
            cart.setItems(items);
            mockMvc.perform(post("/api/carts")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(cart)))
                    .andExpect(status().isOk());
        }
        // Optionally, assert statistics endpoint returns something reasonable
        String offerId = offerIds[random.nextInt(offerIds.length)];
        String action = actions[random.nextInt(actions.length)].name();
        Instant from = Instant.now().minus(1, java.time.temporal.ChronoUnit.DAYS);
        Instant to = Instant.now().plus(1, java.time.temporal.ChronoUnit.DAYS);
        String url = String.format("/api/carts/statistics?offerId=%s&action=%s&from=%s&to=%s", offerId, action, from.toString(), to.toString());
        mockMvc.perform(get(url)).andExpect(status().isOk());
    }

    @Test
    void deleteRandomCartsForStatistics() throws Exception {
        for (int i = 0; i < 30; i++) {
            String customerId = "user" + i;
            mockMvc.perform(delete("/api/carts/" + customerId))
                    .andExpect(status().isNoContent());
        }
    }
}
