package com.xin.payment.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "payment_event")
public class PaymentEvent {
    @Id
    private UUID id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(nullable = false, length = 64)
    private String type;

    @Column(nullable = false, length = 256)
    private String detail;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PaymentEvent() {
    }

    private PaymentEvent(UUID paymentId, String type, String detail) {
        this.id = UUID.randomUUID();
        this.paymentId = Objects.requireNonNull(paymentId);
        this.type = Objects.requireNonNull(type);
        this.detail = Objects.requireNonNull(detail);
        this.createdAt = Instant.now();
    }

    public static PaymentEvent of(UUID paymentId, String type, String detail) {
        return new PaymentEvent(paymentId, type, detail);
    }

    public UUID id() { return id; }
    public String type() { return type; }
    public String detail() { return detail; }
    public Instant createdAt() { return createdAt; }
}
