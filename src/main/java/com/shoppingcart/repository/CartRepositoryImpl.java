package com.shoppingcart.repository;

import com.shoppingcart.model.Cart;
import com.shoppingcart.model.Item;
import lombok.experimental.FieldNameConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import java.time.Instant;
import java.util.List;

@FieldNameConstants
public class CartRepositoryImpl implements CartRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<Cart> findCartsByItemDynamic(String offerId, String action, Instant from, Instant to) {
        Criteria criteria = Criteria.where(Cart.Fields.items)
            .elemMatch(
                Criteria.where(Item.Fields.offerId).is(offerId)
                    .and(Item.Fields.action).is(action)
                    .and(Item.Fields.actionTimestamp).gt(from).lt(to)
            );
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Cart.class);
    }
}
