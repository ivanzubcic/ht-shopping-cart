package com.shoppingcart.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;
import java.util.List;

@Data
@FieldNameConstants
public class Item {
    @NotNull
    private String offerId;

    @NotNull
    private Action action;

    @NotEmpty
    private List<Price> prices;

    @NotNull
    private Instant actionTimestamp;

    public enum Action {
        ADD, MODIFY, DELETE
    }
}
