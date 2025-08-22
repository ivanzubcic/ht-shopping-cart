package com.shoppingcart.service;

import com.shoppingcart.model.Cart;
import com.shoppingcart.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
class CartServiceTest {
    @MockBean
    private CartRepository cartRepository;

    @Autowired
    private CartService cartService;

    @Test
    void testRetryPatternOnTimeout() {
        String customerId = "user1";
        Cart cart = new Cart();
        cart.setId("cart1");
        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(cart));
        doThrow(new RuntimeException("Timeout"))
            .when(cartRepository).deleteById(cart.getId());

        Exception exception = assertThrows(RuntimeException.class, () -> cartService.deleteCartByCustomerId(customerId));
        assertTrue(exception.getMessage().contains("Service temporarily unavailable"));
        verify(cartRepository, times(3)).deleteById(cart.getId());
    }

    @Test
    void testCircuitBreakerOpensAfterFailures() {
        String customerId = "user2";
        Cart cart = new Cart();
        cart.setId("cart2");
        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(cart));
        doThrow(new RuntimeException("Timeout"))
            .when(cartRepository).deleteById(cart.getId());

        for (int i = 0; i < 10; i++) {
            try {
                cartService.deleteCartByCustomerId(customerId);
            } catch (Exception ignored) {}
        }
        Exception exception = assertThrows(RuntimeException.class, () -> cartService.deleteCartByCustomerId(customerId));
        assertTrue(exception.getMessage().contains("Service temporarily unavailable"));
    }
}
