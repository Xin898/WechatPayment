package com.xin.payment.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentIntentTest {
    @Test
    void followsTheHappyPath() {
        PaymentIntent payment = PaymentIntent.create("ORDER-001", 129900, "cny");

        payment.startProcessing();
        payment.succeed("mock_123");

        assertThat(payment.status()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(payment.currency()).isEqualTo("CNY");
    }

    @Test
    void rejectsAnIllegalTransition() {
        PaymentIntent payment = PaymentIntent.create("ORDER-002", 100, "CNY");

        assertThatThrownBy(() -> payment.succeed("mock_123"))
                .isInstanceOf(IllegalStateException.class);
    }
}
