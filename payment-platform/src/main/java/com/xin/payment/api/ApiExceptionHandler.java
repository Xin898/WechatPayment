package com.xin.payment.api;

import com.xin.payment.application.PaymentService.PaymentNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(PaymentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Map<String, String> notFound(PaymentNotFoundException exception) {
        return Map.of("code", "payment_not_found", "message", exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    Map<String, String> conflict(IllegalStateException exception) {
        return Map.of("code", "invalid_payment_state", "message", exception.getMessage());
    }
}
