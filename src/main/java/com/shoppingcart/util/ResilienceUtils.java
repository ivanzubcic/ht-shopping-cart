package com.shoppingcart.util;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ResilienceUtils {
    private static final String DEFAULT_CIRCUIT_BREAKER = "cartServiceCB";
    private static final String DEFAULT_RETRY = "cartServiceRetry";

    private static ResilienceProperties resilienceProperties;

    @Autowired
    public ResilienceUtils(ResilienceProperties resilienceProperties) {
        ResilienceUtils.resilienceProperties = resilienceProperties;
    }

    private static CircuitBreaker getCircuitBreaker() {
        return CircuitBreakerRegistry.ofDefaults().circuitBreaker(DEFAULT_CIRCUIT_BREAKER);
    }

    private static Retry getRetry() {
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(resilienceProperties.getMaxAttempts())
                .waitDuration(java.time.Duration.ofMillis(resilienceProperties.getInitialWaitMs()))
                .intervalFunction(io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(resilienceProperties.getInitialWaitMs(), resilienceProperties.getMultiplier()))
                .build();
        RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);
        return retryRegistry.retry(DEFAULT_RETRY);
    }

    /**
     * Runs the given Runnable with circuit breaker and exponential backoff retry.
     */
    public static void runWithResilience(Runnable runnable) {
        CircuitBreaker circuitBreaker = getCircuitBreaker();
        Retry retry = getRetry();
        Runnable decorated = Retry.decorateRunnable(
                retry,
                CircuitBreaker.decorateRunnable(circuitBreaker, runnable)
        );
        try {
            decorated.run();
        } catch (Exception | Error t) {
            throw new RuntimeException("Service temporarily unavailable. Please try again later.", t);
        }
    }

    /**
     * Runs the given Supplier with circuit breaker and exponential backoff retry and returns its value.
     */
    public static <T> T callWithResilience(Supplier<T> supplier) {
        CircuitBreaker circuitBreaker = getCircuitBreaker();
        Retry retry = getRetry();
        Supplier<T> decorated = Retry.decorateSupplier(
                retry,
                CircuitBreaker.decorateSupplier(circuitBreaker, supplier)
        );
        try {
            return decorated.get();
        } catch (Exception | Error t) {
            throw new RuntimeException("Service temporarily unavailable. Please try again later.", t);
        }
    }
}
