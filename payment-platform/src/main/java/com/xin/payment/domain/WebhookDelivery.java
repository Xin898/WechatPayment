package com.xin.payment.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name="webhook_delivery")
public class WebhookDelivery {
    public enum Status { PENDING, RETRYING, SUCCEEDED, DEAD }
    @Id private UUID id;
    @Column(name="outbox_event_id", nullable=false, unique=true) private UUID outboxEventId;
    @Column(nullable=false, length=256) private String endpoint;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=16) private Status status;
    @Column(nullable=false) private int attempts;
    @Column(name="failures_before_success", nullable=false) private int failuresBeforeSuccess;
    @Column(name="next_attempt_at", nullable=false) private Instant nextAttemptAt;
    @Column(name="last_status_code") private Integer lastStatusCode;
    @Column(name="last_error", length=256) private String lastError;
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @Column(name="updated_at", nullable=false) private Instant updatedAt;

    protected WebhookDelivery() {}
    public static WebhookDelivery demo(UUID outboxEventId) {
        WebhookDelivery d=new WebhookDelivery(); d.id=UUID.randomUUID(); d.outboxEventId=outboxEventId; d.endpoint="mock://merchant/webhooks";
        d.status=Status.PENDING; d.attempts=0; d.failuresBeforeSuccess=2; d.createdAt=Instant.now(); d.updatedAt=d.createdAt; d.nextAttemptAt=d.createdAt; return d;
    }
    public void attempt() {
        attempts++; updatedAt=Instant.now();
        if (attempts <= failuresBeforeSuccess) { status=attempts>=5?Status.DEAD:Status.RETRYING; lastStatusCode=503; lastError="mock endpoint unavailable"; nextAttemptAt=Instant.now().plus(attempts, ChronoUnit.SECONDS); }
        else { status=Status.SUCCEEDED; lastStatusCode=200; lastError=null; nextAttemptAt=Instant.now(); }
    }
    public void retryNow() { if(status==Status.SUCCEEDED) return; status=Status.RETRYING; nextAttemptAt=Instant.now(); updatedAt=nextAttemptAt; }
    public UUID id(){return id;} public UUID outboxEventId(){return outboxEventId;} public String endpoint(){return endpoint;}
    public Status status(){return status;} public int attempts(){return attempts;} public Instant nextAttemptAt(){return nextAttemptAt;}
    public Integer lastStatusCode(){return lastStatusCode;} public String lastError(){return lastError;} public Instant updatedAt(){return updatedAt;}
}
