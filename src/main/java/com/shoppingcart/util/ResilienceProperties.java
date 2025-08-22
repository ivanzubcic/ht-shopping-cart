package com.shoppingcart.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "resilience")
public class ResilienceProperties {
    private int initialWaitMs;
    private double multiplier;
    private int maxAttempts;
}
