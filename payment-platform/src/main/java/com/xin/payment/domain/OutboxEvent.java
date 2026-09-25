package com.xin.payment.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
public class OutboxEvent {
    public enum Status { PENDING, PUBLISHED, FAILED }
    @Id private UUID id;
    @Column(name="aggregate_type", nullable=false, length=32) private String aggregateType;
    @Column(name="aggregate_id", nullable=false) private UUID aggregateId;
    @Column(name="event_type", nullable=false, length=64) private String eventType;
    @Column(nullable=false, columnDefinition="TEXT") private String payload;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=16) private Status status;
    @Column(nullable=false) private int attempts;
    @Column(name="available_at", nullable=false) private Instant availableAt;
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @Column(name="published_at") private Instant publishedAt;
    @Column(name="last_error", length=256) private String lastError;

    protected OutboxEvent() {}
    public static OutboxEvent pending(String aggregateType, UUID aggregateId, String eventType, String payload) {
        OutboxEvent event = new OutboxEvent(); event.id=UUID.randomUUID(); event.aggregateType=aggregateType;
        event.aggregateId=aggregateId; event.eventType=eventType; event.payload=payload; event.status=Status.PENDING;
        event.attempts=0; event.createdAt=Instant.now(); event.availableAt=event.createdAt; return event;
    }
    public void published() { attempts++; status=Status.PUBLISHED; publishedAt=Instant.now(); lastError=null; }
    public UUID id(){return id;} public String aggregateType(){return aggregateType;} public UUID aggregateId(){return aggregateId;}
    public String eventType(){return eventType;} public String payload(){return payload;} public Status status(){return status;}
    public int attempts(){return attempts;} public Instant createdAt(){return createdAt;} public Instant publishedAt(){return publishedAt;}
}
