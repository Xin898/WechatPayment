package com.xin.payment.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "payment_intent")
public class PaymentIntent {
    @Id
    private UUID id;

    @Column(name = "merchant_order_id", nullable = false, length = 128, unique = true)
    private String merchantOrderId;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status;

    @Column(name = "provider_reference", length = 128)
    private String providerReference;

    @Column(name = "failure_code", length = 128)
    private String failureCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected PaymentIntent() {
    }

    private PaymentIntent(UUID id, String merchantOrderId, long amount, String currency) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
        this.id = Objects.requireNonNull(id);
        this.merchantOrderId = Objects.requireNonNull(merchantOrderId);
        this.amount = amount;
        this.currency = Objects.requireNonNull(currency).toUpperCase(Locale.ROOT);
        this.status = PaymentStatus.REQUIRES_CONFIRMATION;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public static PaymentIntent create(String merchantOrderId, long amount, String currency) {
        return new PaymentIntent(UUID.randomUUID(), merchantOrderId, amount, currency);
    }

    public void startProcessing() {
        requireStatus(PaymentStatus.REQUIRES_CONFIRMATION);
        status = PaymentStatus.PROCESSING;
        touch();
    }

    public void remainProcessing(String providerReference) {
        requireStatus(PaymentStatus.PROCESSING);
        this.providerReference = Objects.requireNonNull(providerReference);
        touch();
    }

    public void succeed(String providerReference) {
        requireStatus(PaymentStatus.PROCESSING);
        this.providerReference = Objects.requireNonNull(providerReference);
        status = PaymentStatus.SUCCEEDED;
        touch();
    }

    public void fail(String failureCode) {
        requireStatus(PaymentStatus.PROCESSING);
        this.failureCode = Objects.requireNonNull(failureCode);
        status = PaymentStatus.FAILED;
        touch();
    }

    private void requireStatus(PaymentStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Expected " + expected + " but was " + status);
        }
    }

    private void touch() {
        updatedAt = Instant.now();
    }

    public UUID id() { return id; }
    public String merchantOrderId() { return merchantOrderId; }
    public long amount() { return amount; }
    public String currency() { return currency; }
    public PaymentStatus status() { return status; }
    public String providerReference() { return providerReference; }
    public String failureCode() { return failureCode; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
