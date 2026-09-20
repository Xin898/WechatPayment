package com.xin.payment.application;

import com.xin.payment.domain.PaymentIntent;
import com.xin.payment.provider.PaymentProvider;
import com.xin.payment.provider.ProviderResult;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentService {
    private final PaymentProvider paymentProvider;
    private final Map<UUID, PaymentIntent> payments = new ConcurrentHashMap<>();
    private final Map<String, UUID> idempotencyKeys = new ConcurrentHashMap<>();

    public PaymentService(PaymentProvider paymentProvider) {
        this.paymentProvider = paymentProvider;
    }

    public PaymentIntent create(
            String idempotencyKey,
            String merchantOrderId,
            long amount,
            String currency
    ) {
        UUID existingId = idempotencyKeys.get(idempotencyKey);
        if (existingId != null) return get(existingId);

        PaymentIntent payment = PaymentIntent.create(merchantOrderId, amount, currency);
        UUID winner = idempotencyKeys.putIfAbsent(idempotencyKey, payment.id());
        if (winner != null) return get(winner);

        payments.put(payment.id(), payment);
        return payment;
    }

    public PaymentIntent confirm(UUID paymentId) {
        PaymentIntent payment = get(paymentId);
        payment.startProcessing();

        ProviderResult result = paymentProvider.confirm(payment);
        switch (result.outcome()) {
            case SUCCEEDED -> payment.succeed(result.providerReference());
            case FAILED -> payment.fail(result.failureCode());
            case PROCESSING -> { /* callback or reconciliation will resolve it */ }
        }
        return payment;
    }

    public PaymentIntent get(UUID paymentId) {
        PaymentIntent payment = payments.get(paymentId);
        if (payment == null) throw new PaymentNotFoundException(paymentId);
        return payment;
    }

    public static final class PaymentNotFoundException extends RuntimeException {
        public PaymentNotFoundException(UUID id) {
            super("Payment intent not found: " + id);
        }
    }
}
