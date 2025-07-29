package com.shoppingcart.model;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class Price {
    @NotNull
    private PriceType type;

    @NotNull
    private BigDecimal value;

    // Only for recurring price
    private Integer recurrences;

    public enum PriceType {
        RECURRING, ONE_TIME
    }
}
