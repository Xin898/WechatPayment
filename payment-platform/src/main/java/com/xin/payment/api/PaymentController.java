package com.xin.payment.api;

import com.xin.payment.application.PaymentService;
import com.xin.payment.domain.PaymentIntent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/v1/payment-intents")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse create(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        return PaymentResponse.from(paymentService.create(
                idempotencyKey,
                request.merchantOrderId(),
                request.amount(),
                request.currency()
        ));
    }

    @PostMapping("/{id}/confirm")
    public PaymentResponse confirm(@PathVariable UUID id) {
        return PaymentResponse.from(paymentService.confirm(id));
    }

    @GetMapping("/{id}")
    public PaymentResponse get(@PathVariable UUID id) {
        return PaymentResponse.from(paymentService.get(id));
    }

    public record CreatePaymentRequest(
            @NotBlank String merchantOrderId,
            @Positive long amount,
            @NotBlank String currency
    ) {}

    public record PaymentResponse(
            UUID id,
            String merchantOrderId,
            long amount,
            String currency,
            String status,
            String providerReference,
            String failureCode,
            Instant createdAt
    ) {
        static PaymentResponse from(PaymentIntent payment) {
            return new PaymentResponse(
                    payment.id(),
                    payment.merchantOrderId(),
                    payment.amount(),
                    payment.currency(),
                    payment.status().name(),
                    payment.providerReference(),
                    payment.failureCode(),
                    payment.createdAt()
            );
        }
    }
}
