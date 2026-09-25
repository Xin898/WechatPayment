package com.xin.payment.application;

import com.xin.payment.domain.OutboxEvent;
import com.xin.payment.domain.WebhookDelivery;
import com.xin.payment.infrastructure.OutboxEventRepository;
import com.xin.payment.infrastructure.WebhookDeliveryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OutboxService {
    private final OutboxEventRepository outbox;
    private final WebhookDeliveryRepository webhooks;
    public OutboxService(OutboxEventRepository outbox, WebhookDeliveryRepository webhooks) { this.outbox=outbox; this.webhooks=webhooks; }

    public OutboxEvent append(String aggregateType, UUID aggregateId, String type, String payload) {
        return outbox.save(OutboxEvent.pending(aggregateType, aggregateId, type, payload));
    }

    @Scheduled(fixedDelayString="${payment.outbox.poll-interval-ms:1500}")
    @Transactional
    public void publishPending() {
        for (OutboxEvent event : outbox.findTop50ByStatusAndAvailableAtLessThanEqualOrderByCreatedAtAsc(OutboxEvent.Status.PENDING, Instant.now())) {
            webhooks.findByOutboxEventId(event.id()).orElseGet(() -> webhooks.save(WebhookDelivery.demo(event.id())));
            event.published();
        }
    }

    @Scheduled(fixedDelayString="${payment.webhook.poll-interval-ms:2000}")
    @Transactional
    public void deliverWebhooks() {
        var ready = List.of(WebhookDelivery.Status.PENDING, WebhookDelivery.Status.RETRYING);
        for (WebhookDelivery delivery : webhooks.findTop50ByStatusInAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(ready, Instant.now())) delivery.attempt();
    }

    @Transactional
    public WebhookDelivery retry(UUID id) {
        WebhookDelivery delivery = webhooks.findById(id).orElseThrow(() -> new IllegalArgumentException("Webhook delivery not found: " + id));
        delivery.retryNow(); return delivery;
    }
    @Transactional(readOnly=true) public List<OutboxEvent> events(){return outbox.findTop50ByOrderByCreatedAtDesc();}
    @Transactional(readOnly=true) public List<WebhookDelivery> deliveries(){return webhooks.findTop50ByOrderByCreatedAtDesc();}
}
