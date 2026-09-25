package com.xin.payment.api;

import com.xin.payment.application.PaymentService.IdempotencyConflictException;
import com.xin.payment.application.PaymentService.PaymentNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(PaymentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Map<String, String> notFound(PaymentNotFoundException exception) {
        return error("payment_not_found", exception.getMessage());
    }

    @ExceptionHandler({IllegalStateException.class, IdempotencyConflictException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    Map<String, String> conflict(RuntimeException exception) {
        String code = exception instanceof IdempotencyConflictException
                ? "idempotency_conflict"
                : "invalid_payment_state";
        return error(code, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, String> validation(MethodArgumentNotValidException exception) {
        return error("invalid_request", "Request validation failed");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, String> badRequest(IllegalArgumentException exception) {
        return error("invalid_request", exception.getMessage());
    }

    private static Map<String, String> error(String code, String message) {
        return Map.of("code", code, "message", message);
    }
}
