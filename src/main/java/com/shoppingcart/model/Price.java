package com.shoppingcart.model;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class Price {
    @NotNull
    private PriceType type;

    @NotNull
    private BigDecimal value;

    // Only for recurring price
    private Integer recurrences;

    // Getters and setters
    public PriceType getType() { return type; }
    public void setType(PriceType type) { this.type = type; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
    public Integer getRecurrences() { return recurrences; }
    public void setRecurrences(Integer recurrences) { this.recurrences = recurrences; }

    public enum PriceType {
        RECURRING, ONE_TIME
    }
}
