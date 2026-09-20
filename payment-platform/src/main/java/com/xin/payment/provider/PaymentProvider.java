package com.xin.payment.provider;

import com.xin.payment.domain.PaymentIntent;

public interface PaymentProvider {
    ProviderResult confirm(PaymentIntent paymentIntent);
}
