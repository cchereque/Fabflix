package com.github.klefstad_teaching.cs122b.billing.CustomClasses;

public class PaymentId {
    private String paymentIntentId;

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public PaymentId setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
        return this;
    }
}
