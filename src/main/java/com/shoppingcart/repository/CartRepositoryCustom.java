package com.shoppingcart.repository;

import com.shoppingcart.model.Cart;
import java.time.Instant;
import java.util.List;

public interface CartRepositoryCustom {
    List<Cart> findCartsByItemDynamic(String offerId, String action, Instant from, Instant to);
}
