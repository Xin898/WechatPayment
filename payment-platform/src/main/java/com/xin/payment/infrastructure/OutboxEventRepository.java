package com.xin.payment.infrastructure;

import com.xin.payment.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findTop50ByStatusAndAvailableAtLessThanEqualOrderByCreatedAtAsc(OutboxEvent.Status status, Instant now);
    List<OutboxEvent> findTop50ByOrderByCreatedAtDesc();
}
