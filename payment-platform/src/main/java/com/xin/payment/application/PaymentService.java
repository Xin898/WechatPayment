package com.xin.payment.application;

import com.xin.payment.domain.*;
import com.xin.payment.infrastructure.*;
import com.xin.payment.provider.PaymentProvider;
import com.xin.payment.provider.ProviderResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentProvider paymentProvider;
    private final PaymentIntentRepository payments;
    private final IdempotencyRecordRepository idempotencyRecords;
    private final PaymentEventRepository events;

    public PaymentService(
            PaymentProvider paymentProvider,
            PaymentIntentRepository payments,
            IdempotencyRecordRepository idempotencyRecords,
            PaymentEventRepository events
    ) {
        this.paymentProvider = paymentProvider;
        this.payments = payments;
        this.idempotencyRecords = idempotencyRecords;
        this.events = events;
    }

    @Transactional
    public synchronized PaymentIntent create(
            String idempotencyKey,
            String merchantOrderId,
            long amount,
            String currency
    ) {
        String requestHash = requestHash(merchantOrderId, amount, currency);
        var existing = idempotencyRecords.findById(idempotencyKey);
        if (existing.isPresent()) {
            if (!existing.get().requestHash().equals(requestHash)) {
                throw new IdempotencyConflictException(idempotencyKey);
            }
            return get(existing.get().paymentId());
        }

        PaymentIntent payment = payments.save(
                PaymentIntent.create(merchantOrderId, amount, currency)
        );
        idempotencyRecords.save(
                IdempotencyRecord.create(idempotencyKey, requestHash, payment.id())
        );
        events.save(PaymentEvent.of(
                payment.id(),
                "payment.created",
                "Payment intent created and awaiting confirmation"
        ));
        return payment;
    }

    @Transactional
    public PaymentIntent confirm(UUID paymentId) {
        PaymentIntent payment = get(paymentId);
        if (payment.status() != PaymentStatus.REQUIRES_CONFIRMATION) {
            return payment;
        }

        payment.startProcessing();
        events.save(PaymentEvent.of(
                payment.id(),
                "payment.processing",
                "Request sent to mock payment provider"
        ));

        ProviderResult result = paymentProvider.confirm(payment);
        switch (result.outcome()) {
            case SUCCEEDED -> {
                payment.succeed(result.providerReference());
                events.save(PaymentEvent.of(
                        payment.id(),
                        "payment.succeeded",
                        "Provider confirmed the payment"
                ));
            }
            case FAILED -> {
                payment.fail(result.failureCode());
                events.save(PaymentEvent.of(
                        payment.id(),
                        "payment.failed",
                        "Provider declined the payment: " + result.failureCode()
                ));
            }
            case PROCESSING -> {
                payment.remainProcessing(result.providerReference());
                events.save(PaymentEvent.of(
                        payment.id(),
                        "payment.pending",
                        "Provider result is unknown; callback or reconciliation must resolve it"
                ));
            }
        }
        return payments.save(payment);
    }

    @Transactional(readOnly = true)
    public PaymentIntent get(UUID paymentId) {
        return payments.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    @Transactional(readOnly = true)
    public List<PaymentEvent> events(UUID paymentId) {
        get(paymentId);
        return events.findByPaymentIdOrderByCreatedAtAsc(paymentId);
    }

    private static String requestHash(String merchantOrderId, long amount, String currency) {
        String normalized = merchantOrderId + "\u001f" + amount + "\u001f"
                + currency.toUpperCase(Locale.ROOT);
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    public static final class PaymentNotFoundException extends RuntimeException {
        public PaymentNotFoundException(UUID id) {
            super("Payment intent not found: " + id);
        }
    }

    public static final class IdempotencyConflictException extends RuntimeException {
        public IdempotencyConflictException(String key) {
            super("Idempotency key was already used with different parameters: " + key);
        }
    }
}
