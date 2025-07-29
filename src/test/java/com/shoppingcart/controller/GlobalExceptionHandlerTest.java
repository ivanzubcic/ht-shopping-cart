package com.shoppingcart.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleIllegalStateException() {
        IllegalStateException ex = new IllegalStateException("Test conflict");
        WebRequest request = mock(WebRequest.class);
        ResponseEntity<Object> response = handler.handleIllegalStateException(ex, request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Map<?,?> body = (Map<?,?>) response.getBody();
        assertEquals("Conflict", body.get("error"));
        assertEquals("Test conflict", body.get("message"));
    }

    @Test
    void testHandleNoSuchElementException() {
        java.util.NoSuchElementException ex = new java.util.NoSuchElementException("Not found");
        WebRequest request = mock(WebRequest.class);
        ResponseEntity<Object> response = handler.handleNoSuchElementException(ex, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<?,?> body = (Map<?,?>) response.getBody();
        assertEquals("Not Found", body.get("error"));
        assertEquals("Not found", body.get("message"));
    }

    @Test
    void testHandleValidationExceptions() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(TestUtils.mockBindingResultWithFieldError("customerId", "must not be null"));
        ResponseEntity<Object> response = handler.handleValidationExceptions(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<?,?> body = (Map<?,?>) response.getBody();
        assertEquals("Validation Failed", body.get("error"));
        Map<?,?> details = (Map<?,?>) body.get("details");
        assertEquals("must not be null", details.get("customerId"));
    }

    @Test
    void testHandleAllExceptions() {
        Exception ex = new Exception("Internal error");
        WebRequest request = mock(WebRequest.class);
        ResponseEntity<Object> response = handler.handleAllExceptions(ex, request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<?,?> body = (Map<?,?>) response.getBody();
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals("Internal error", body.get("message"));
    }
}
