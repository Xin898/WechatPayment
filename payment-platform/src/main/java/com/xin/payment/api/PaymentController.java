package com.xin.payment.api;

import com.xin.payment.application.PaymentService;
import com.xin.payment.domain.PaymentEvent;
import com.xin.payment.domain.PaymentIntent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
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
            @RequestHeader("Idempotency-Key") @NotBlank @Size(max = 128) String idempotencyKey,
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

    @GetMapping("/{id}/events")
    public List<EventResponse> events(@PathVariable UUID id) {
        return paymentService.events(id).stream().map(EventResponse::from).toList();
    }

    public record CreatePaymentRequest(
            @NotBlank @Size(max = 128) String merchantOrderId,
            @Positive long amount,
            @NotBlank @Size(min = 3, max = 3) String currency
    ) {}

    public record PaymentResponse(
            UUID id,
            String merchantOrderId,
            long amount,
            String currency,
            String status,
            String providerReference,
            String failureCode,
            Instant createdAt,
            Instant updatedAt
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
                    payment.createdAt(),
                    payment.updatedAt()
            );
        }
    }

    public record EventResponse(UUID id, String type, String detail, Instant createdAt) {
        static EventResponse from(PaymentEvent event) {
            return new EventResponse(event.id(), event.type(), event.detail(), event.createdAt());
        }
    }
}
