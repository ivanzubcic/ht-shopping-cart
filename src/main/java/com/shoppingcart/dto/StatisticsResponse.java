package com.shoppingcart.dto;

import java.time.Instant;

public record StatisticsResponse(
    String offerId,
    String action,
    Instant from,
    Instant to,
    long count,
    long uniqueCustomers,
    long totalItems
) {}
