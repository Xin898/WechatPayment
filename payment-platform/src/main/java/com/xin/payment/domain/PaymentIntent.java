package com.xin.payment.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class PaymentIntent {
    private final UUID id;
    private final String merchantOrderId;
    private final long amount;
    private final String currency;
    private final Instant createdAt;
    private PaymentStatus status;
    private String providerReference;
    private String failureCode;

    private PaymentIntent(UUID id, String merchantOrderId, long amount, String currency) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
        this.id = Objects.requireNonNull(id);
        this.merchantOrderId = Objects.requireNonNull(merchantOrderId);
        this.amount = amount;
        this.currency = Objects.requireNonNull(currency).toUpperCase();
        this.createdAt = Instant.now();
        this.status = PaymentStatus.REQUIRES_CONFIRMATION;
    }

    public static PaymentIntent create(String merchantOrderId, long amount, String currency) {
        return new PaymentIntent(UUID.randomUUID(), merchantOrderId, amount, currency);
    }

    public synchronized void startProcessing() {
        requireStatus(PaymentStatus.REQUIRES_CONFIRMATION);
        status = PaymentStatus.PROCESSING;
    }

    public synchronized void succeed(String providerReference) {
        requireStatus(PaymentStatus.PROCESSING);
        this.providerReference = Objects.requireNonNull(providerReference);
        status = PaymentStatus.SUCCEEDED;
    }

    public synchronized void fail(String failureCode) {
        requireStatus(PaymentStatus.PROCESSING);
        this.failureCode = Objects.requireNonNull(failureCode);
        status = PaymentStatus.FAILED;
    }

    private void requireStatus(PaymentStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Expected " + expected + " but was " + status);
        }
    }

    public UUID id() { return id; }
    public String merchantOrderId() { return merchantOrderId; }
    public long amount() { return amount; }
    public String currency() { return currency; }
    public Instant createdAt() { return createdAt; }
    public PaymentStatus status() { return status; }
    public String providerReference() { return providerReference; }
    public String failureCode() { return failureCode; }
}
