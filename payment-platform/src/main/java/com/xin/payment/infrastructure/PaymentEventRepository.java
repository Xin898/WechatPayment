package com.xin.payment.infrastructure;

import com.xin.payment.domain.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentEventRepository extends JpaRepository<PaymentEvent, UUID> {
    List<PaymentEvent> findByPaymentIdOrderByCreatedAtAsc(UUID paymentId);
}
