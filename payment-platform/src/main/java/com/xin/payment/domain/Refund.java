package com.xin.payment.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refund")
public class Refund {
    public enum Status { PENDING, SUCCEEDED, FAILED }

    @Id private UUID id;
    @Column(name = "payment_id", nullable = false) private UUID paymentId;
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 128) private String idempotencyKey;
    @Column(nullable = false) private long amount;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private Status status;
    @Column(name = "failure_code", length = 128) private String failureCode;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version @Column(nullable = false) private long version;

    protected Refund() {}

    public static Refund pending(UUID paymentId, String key, long amount) {
        if (amount <= 0) throw new IllegalArgumentException("Refund amount must be positive");
        Refund refund = new Refund();
        refund.id = UUID.randomUUID();
        refund.paymentId = paymentId;
        refund.idempotencyKey = key;
        refund.amount = amount;
        refund.status = Status.PENDING;
        refund.createdAt = Instant.now();
        refund.updatedAt = refund.createdAt;
        return refund;
    }

    public void succeed() { require(Status.PENDING); status = Status.SUCCEEDED; updatedAt = Instant.now(); }
    public void fail(String code) { require(Status.PENDING); status = Status.FAILED; failureCode = code; updatedAt = Instant.now(); }
    private void require(Status expected) { if (status != expected) throw new IllegalStateException("Expected " + expected + " but was " + status); }

    public UUID id() { return id; }
    public UUID paymentId() { return paymentId; }
    public long amount() { return amount; }
    public Status status() { return status; }
    public String failureCode() { return failureCode; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
