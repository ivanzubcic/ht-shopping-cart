package com.shoppingcart.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

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

    // Getters and setters
    public String getOfferId() { return offerId; }
    public void setOfferId(String offerId) { this.offerId = offerId; }
    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }
    public List<Price> getPrices() { return prices; }
    public void setPrices(List<Price> prices) { this.prices = prices; }
    public Instant getActionTimestamp() { return actionTimestamp; }
    public void setActionTimestamp(Instant actionTimestamp) { this.actionTimestamp = actionTimestamp; }

    public enum Action {
        ADD, MODIFY, DELETE
    }
}
