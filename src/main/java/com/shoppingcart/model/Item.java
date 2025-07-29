package com.shoppingcart.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class Item {
    @NotNull
    private String offerId;

    @NotNull
    private Action action;

    @NotEmpty
    private List<Price> prices;

    @NotNull
    @JsonProperty(access = Access.READ_ONLY)
    private Instant actionTimestamp;

    public enum Action {
        ADD, MODIFY, DELETE
    }
}
