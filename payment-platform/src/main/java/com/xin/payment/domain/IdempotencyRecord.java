package com.xin.payment.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "idempotency_record")
public class IdempotencyRecord {
    @Id
    @Column(name = "idempotency_key", length = 128)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected IdempotencyRecord() {
    }

    private IdempotencyRecord(String idempotencyKey, String requestHash, UUID paymentId) {
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey);
        this.requestHash = Objects.requireNonNull(requestHash);
        this.paymentId = Objects.requireNonNull(paymentId);
        this.createdAt = Instant.now();
    }

    public static IdempotencyRecord create(String key, String requestHash, UUID paymentId) {
        return new IdempotencyRecord(key, requestHash, paymentId);
    }

    public String requestHash() { return requestHash; }
    public UUID paymentId() { return paymentId; }
}
