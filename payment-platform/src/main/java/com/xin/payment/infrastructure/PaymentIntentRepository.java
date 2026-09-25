package com.xin.payment.infrastructure;

import com.xin.payment.domain.PaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, UUID> {
}
