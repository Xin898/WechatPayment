package com.xin.payment.application;

import com.xin.payment.domain.LedgerTransaction;
import com.xin.payment.domain.PaymentIntent;
import com.xin.payment.domain.Refund;
import com.xin.payment.infrastructure.LedgerTransactionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class LedgerService {
    private final LedgerTransactionRepository transactions;
    public LedgerService(LedgerTransactionRepository transactions) { this.transactions = transactions; }

    public LedgerTransaction recordPayment(PaymentIntent payment) {
        long fee = payment.amount() > 1 ? Math.max(1, payment.amount() / 100) : 0;
        LedgerTransaction transaction = LedgerTransaction.create("PAYMENT", payment.id(), "Capture payment")
                .debit("provider_receivable", payment.amount(), payment.currency())
                .credit("merchant_payable", payment.amount() - fee, payment.currency());
        if (fee > 0) transaction.credit("platform_fee_revenue", fee, payment.currency());
        transaction.requireBalanced();
        return transactions.save(transaction);
    }

    public LedgerTransaction recordRefund(Refund refund, PaymentIntent payment) {
        LedgerTransaction transaction = LedgerTransaction.create("REFUND", refund.id(), "Refund payment " + payment.id())
                .debit("merchant_payable", refund.amount(), payment.currency())
                .credit("provider_receivable", refund.amount(), payment.currency());
        transaction.requireBalanced();
        return transactions.save(transaction);
    }

    public List<LedgerTransaction> forReference(UUID referenceId) { return transactions.findByReferenceIdOrderByCreatedAtDesc(referenceId); }
}
