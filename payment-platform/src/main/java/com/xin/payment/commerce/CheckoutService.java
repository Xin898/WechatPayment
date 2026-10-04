package com.xin.payment.commerce;

import com.xin.payment.application.PaymentService;
import com.xin.payment.application.OutboxService;
import com.xin.payment.domain.PaymentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class CheckoutService {
    private final CartRepository carts; private final OrderRepository orders; private final InventoryService inventory;
    private final VoucherService vouchers; private final PaymentService payments; private final OutboxService outbox;
    public CheckoutService(CartRepository carts,OrderRepository orders,InventoryService inventory,VoucherService vouchers,PaymentService payments,OutboxService outbox){this.carts=carts;this.orders=orders;this.inventory=inventory;this.vouchers=vouchers;this.payments=payments;this.outbox=outbox;}

    @Transactional
    public synchronized SalesOrder checkout(UUID cartId,String key,long requestedVoucher){
        var existing=orders.findByIdempotencyKey(key);if(existing.isPresent())return existing.get();
        ShoppingCart cart=carts.findById(cartId).orElseThrow(()->new IllegalArgumentException("Cart not found: "+cartId));
        if(cart.status()!=ShoppingCart.Status.OPEN)throw new IllegalStateException("Cart is not open");
        inventory.reserve(cart.items());
        long voucher=Math.min(Math.min(Math.max(0,requestedVoucher),cart.total()),vouchers.available(cart.customerId()));
        SalesOrder order=orders.save(SalesOrder.create(cart,key,voucher));
        vouchers.spend(cart.customerId(),voucher,"order:"+order.orderNumber());
        outbox.append("ORDER",order.id(),"order.created","{\"orderId\":\""+order.id()+"\",\"total\":"+order.totalAmount()+"}");
        if(order.payableAmount()==0){cart.checkout();inventory.commit(order.lines());outbox.append("ORDER",order.id(),"order.paid","{\"orderId\":\""+order.id()+"\",\"method\":\"VOUCHER\"}");return order;}
        var payment=payments.create("checkout-payment:"+key,order.orderNumber(),order.payableAmount(),order.currency());
        order.attachPayment(payment.id());
        return order;
    }

    @Transactional
    public SalesOrder confirm(UUID orderId){
        SalesOrder order=get(orderId);if(order.status()!=SalesOrder.Status.PENDING_PAYMENT)return order;
        var payment=payments.confirm(order.paymentId());
        if(payment.status()==PaymentStatus.SUCCEEDED){order.paid();inventory.commit(order.lines());carts.findById(order.cartId()).orElseThrow().checkout();outbox.append("ORDER",order.id(),"order.paid","{\"orderId\":\""+order.id()+"\",\"method\":\"MIXED\"}");}
        else if(payment.status()==PaymentStatus.FAILED){order.paymentFailed();inventory.release(order.lines());vouchers.refund(order.customerId(),order.voucherAmount(),"payment-failed:"+order.orderNumber());outbox.append("ORDER",order.id(),"order.payment_failed","{\"orderId\":\""+order.id()+"\"}");}
        return order;
    }
    @Transactional(readOnly=true) public SalesOrder get(UUID id){return orders.findById(id).orElseThrow(()->new IllegalArgumentException("Order not found: "+id));}
}
