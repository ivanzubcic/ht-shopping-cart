package com.shoppingcart.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Document(collection = "carts")
@Data
@FieldNameConstants
public class Cart {
    @Id
    private String id;

    @NotNull
    private String customerId;

    @NotEmpty
    private List<Item> items;
}
