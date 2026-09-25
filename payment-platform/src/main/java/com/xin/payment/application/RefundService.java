package com.xin.payment.application;

import com.xin.payment.domain.PaymentEvent;
import com.xin.payment.domain.Refund;
import com.xin.payment.infrastructure.PaymentEventRepository;
import com.xin.payment.infrastructure.PaymentIntentRepository;
import com.xin.payment.infrastructure.RefundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class RefundService {
    private final PaymentIntentRepository payments; private final RefundRepository refunds;
    private final PaymentEventRepository events; private final LedgerService ledger; private final OutboxService outbox;
    public RefundService(PaymentIntentRepository payments, RefundRepository refunds, PaymentEventRepository events, LedgerService ledger, OutboxService outbox) {
        this.payments=payments; this.refunds=refunds; this.events=events; this.ledger=ledger; this.outbox=outbox;
    }

    @Transactional
    public synchronized Refund create(UUID paymentId, String key, long amount) {
        var existing = refunds.findByIdempotencyKey(key);
        if (existing.isPresent()) {
            Refund refund=existing.get();
            if(!refund.paymentId().equals(paymentId) || refund.amount()!=amount) throw new PaymentService.IdempotencyConflictException(key);
            return refund;
        }
        var payment=payments.findById(paymentId).orElseThrow(() -> new PaymentService.PaymentNotFoundException(paymentId));
        Refund refund=refunds.save(Refund.pending(paymentId, key, amount));
        events.save(PaymentEvent.of(paymentId, "refund.pending", "Refund " + refund.id() + " submitted"));
        payment.applyRefund(amount);
        refund.succeed();
        ledger.recordRefund(refund, payment);
        events.save(PaymentEvent.of(paymentId, "refund.succeeded", "Refund completed for " + amount + " " + payment.currency()));
        outbox.append("REFUND", refund.id(), "refund.succeeded", "{\"refundId\":\""+refund.id()+"\",\"paymentId\":\""+paymentId+"\",\"amount\":"+amount+"}");
        return refund;
    }
    @Transactional(readOnly=true) public List<Refund> forPayment(UUID paymentId){return refunds.findByPaymentIdOrderByCreatedAtAsc(paymentId);}
}
