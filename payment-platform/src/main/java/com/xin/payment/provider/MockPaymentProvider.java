package com.xin.payment.provider;

import com.xin.payment.domain.PaymentIntent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockPaymentProvider implements PaymentProvider {
    @Override
    public ProviderResult confirm(PaymentIntent paymentIntent) {
        long suffix = paymentIntent.amount() % 100;
        if (suffix == 2) {
            return ProviderResult.failed("mock_payment_declined");
        }
        String reference = "mock_" + UUID.randomUUID();
        if (suffix == 77) {
            return ProviderResult.processing(reference);
        }
        return ProviderResult.succeeded(reference);
    }
}
