package com.xin.payment.api;

import com.xin.payment.application.LedgerService;
import com.xin.payment.application.OutboxService;
import com.xin.payment.application.RefundService;
import com.xin.payment.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
public class ReliabilityController {
    private final RefundService refunds; private final LedgerService ledger; private final OutboxService outbox;
    public ReliabilityController(RefundService refunds, LedgerService ledger, OutboxService outbox){this.refunds=refunds;this.ledger=ledger;this.outbox=outbox;}

    @PostMapping("/payment-intents/{paymentId}/refunds")
    RefundResponse refund(@PathVariable UUID paymentId, @RequestHeader("Idempotency-Key") String key, @Valid @RequestBody RefundRequest request){return RefundResponse.from(refunds.create(paymentId,key,request.amount()));}
    @GetMapping("/payment-intents/{paymentId}/refunds")
    List<RefundResponse> refunds(@PathVariable UUID paymentId){return refunds.forPayment(paymentId).stream().map(RefundResponse::from).toList();}
    @GetMapping("/ledger/transactions")
    List<LedgerResponse> ledger(@RequestParam UUID referenceId){return ledger.forReference(referenceId).stream().map(LedgerResponse::from).toList();}
    @GetMapping("/operations/outbox")
    List<OutboxResponse> outbox(){return outbox.events().stream().map(OutboxResponse::from).toList();}
    @GetMapping("/operations/webhooks")
    List<WebhookResponse> webhooks(){return outbox.deliveries().stream().map(WebhookResponse::from).toList();}
    @PostMapping("/operations/webhooks/{id}/retry")
    WebhookResponse retry(@PathVariable UUID id){return WebhookResponse.from(outbox.retry(id));}

    public record RefundRequest(@Positive long amount){}
    public record RefundResponse(UUID id,UUID paymentId,long amount,String status,String failureCode,Instant createdAt,Instant updatedAt){static RefundResponse from(Refund r){return new RefundResponse(r.id(),r.paymentId(),r.amount(),r.status().name(),r.failureCode(),r.createdAt(),r.updatedAt());}}
    public record EntryResponse(String account,String side,long amount,String currency){static EntryResponse from(LedgerEntry e){return new EntryResponse(e.accountCode(),e.side().name(),e.amount(),e.currency());}}
    public record LedgerResponse(UUID id,String referenceType,UUID referenceId,String description,boolean balanced,Instant createdAt,List<EntryResponse> entries){static LedgerResponse from(LedgerTransaction t){return new LedgerResponse(t.id(),t.referenceType(),t.referenceId(),t.description(),true,t.createdAt(),t.entries().stream().map(EntryResponse::from).toList());}}
    public record OutboxResponse(UUID id,String aggregateType,UUID aggregateId,String eventType,String status,int attempts,Instant createdAt,Instant publishedAt){static OutboxResponse from(OutboxEvent e){return new OutboxResponse(e.id(),e.aggregateType(),e.aggregateId(),e.eventType(),e.status().name(),e.attempts(),e.createdAt(),e.publishedAt());}}
    public record WebhookResponse(UUID id,UUID eventId,String endpoint,String status,int attempts,Integer lastStatusCode,String lastError,Instant nextAttemptAt){static WebhookResponse from(WebhookDelivery d){return new WebhookResponse(d.id(),d.outboxEventId(),d.endpoint(),d.status().name(),d.attempts(),d.lastStatusCode(),d.lastError(),d.nextAttemptAt());}}
}
