package com.xin.payment.application;

import com.xin.payment.domain.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentServiceIntegrationTest {
    @Autowired
    PaymentService paymentService;

    @Test
    void returnsTheSamePaymentForTheSameIdempotentRequest() {
        var first = paymentService.create("idem-001", "ORDER-001", 129900, "CNY");
        var second = paymentService.create("idem-001", "ORDER-001", 129900, "CNY");

        assertThat(second.id()).isEqualTo(first.id());
    }

    @Test
    void rejectsReusingAKeyWithDifferentParameters() {
        paymentService.create("idem-002", "ORDER-002", 10000, "CNY");

        assertThatThrownBy(() ->
                paymentService.create("idem-002", "ORDER-002", 20000, "CNY")
        ).isInstanceOf(PaymentService.IdempotencyConflictException.class);
    }

    @Test
    void confirmationIsSafeToRetry() {
        var payment = paymentService.create("idem-003", "ORDER-003", 129900, "CNY");

        var first = paymentService.confirm(payment.id());
        var second = paymentService.confirm(payment.id());

        assertThat(first.status()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(second.status()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(second.providerReference()).isEqualTo(first.providerReference());
    }
}
