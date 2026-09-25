package com.xin.payment.infrastructure;

import com.xin.payment.domain.WebhookDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, UUID> {
    Optional<WebhookDelivery> findByOutboxEventId(UUID outboxEventId);
    List<WebhookDelivery> findTop50ByStatusInAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(List<WebhookDelivery.Status> statuses, Instant now);
    List<WebhookDelivery> findTop50ByOrderByCreatedAtDesc();
}
