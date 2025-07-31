package com.shoppingcart.config;

import com.shoppingcart.service.CartService;
import com.shoppingcart.model.Cart;
import com.shoppingcart.model.Item;
import com.shoppingcart.model.Price;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Profile({"dev", "test"})
public class DataLoader implements CommandLineRunner {

    private final CartService cartService;
    private final Random random = new Random();

    private static final String[] OFFER_IDS = {"OFFER1", "OFFER2", "OFFER3", "OFFER4", "OFFER5"};
    private static final Item.Action[] ACTIONS = Item.Action.values();
    private static final Price.PriceType[] PRICE_TYPES = Price.PriceType.values();

    @Autowired
    public DataLoader(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    public void run(String... args) {
        generateRandomCartsForStatistics(10); // You can adjust the count as needed
    }

    private void generateRandomCartsForStatistics(int count) {
        for (int i = 0; i < count; i++) {
            String customerId = "test_user" + i;
            if (cartService.getCartByCustomerId(customerId).isPresent()) {
                // Skip if cart already exists
                continue;
            }
            Cart cart = new Cart();
            cart.setCustomerId(customerId);
            cart.setItems(generateRandomItems(random.nextInt(3) + 1));
            cartService.saveCart(cart);
        }
    }

    private List<Item> generateRandomItems(int count) {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Item item = new Item();
            item.setOfferId(OFFER_IDS[random.nextInt(OFFER_IDS.length)]);
            item.setAction(ACTIONS[random.nextInt(ACTIONS.length)]);
            item.setPrices(List.of(generateRandomPrice()));
            // Set random actionTimestamp within one week from today
            long now = java.time.Instant.now().getEpochSecond();
            long weekSeconds = 7 * 24 * 60 * 60;
            long randomOffset = (long) (random.nextDouble() * weekSeconds);
            item.setActionTimestamp(java.time.Instant.ofEpochSecond(now - randomOffset));
            items.add(item);
        }
        return items;
    }

    private Price generateRandomPrice() {
        Price price = new Price();
        price.setType(PRICE_TYPES[random.nextInt(PRICE_TYPES.length)]);
        price.setValue(java.math.BigDecimal.valueOf(10 + random.nextInt(90)));
        if (price.getType() == Price.PriceType.RECURRING) {
            price.setRecurrences(1 + random.nextInt(12));
        }
        return price;
    }
}
