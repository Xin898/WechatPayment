package com.xin.payment.domain;

public enum PaymentStatus {
    REQUIRES_CONFIRMATION,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    PARTIALLY_REFUNDED,
    REFUNDED
}
