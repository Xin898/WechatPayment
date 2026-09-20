package com.xin.payment.provider;

public record ProviderResult(Outcome outcome, String providerReference, String failureCode) {
    public enum Outcome { SUCCEEDED, FAILED, PROCESSING }

    public static ProviderResult succeeded(String reference) {
        return new ProviderResult(Outcome.SUCCEEDED, reference, null);
    }

    public static ProviderResult failed(String code) {
        return new ProviderResult(Outcome.FAILED, null, code);
    }

    public static ProviderResult processing(String reference) {
        return new ProviderResult(Outcome.PROCESSING, reference, null);
    }
}
