package com.shoppingcart.controller;

import com.shoppingcart.model.Cart;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

public class TestUtils {
    public static BindingResult mockBindingResultWithFieldError(String field, String message) {
        Cart dummy = new Cart(); // or any object, not important
        BindingResult bindingResult = new BeanPropertyBindingResult(dummy, "cart");
        bindingResult.rejectValue(field, "error.code", message);
        return bindingResult;
    }
}
