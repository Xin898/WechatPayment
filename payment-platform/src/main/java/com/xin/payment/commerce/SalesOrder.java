package com.xin.payment.commerce;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name="sales_order")
public class SalesOrder {
    public enum Status { PENDING_PAYMENT, PAID, PAYMENT_FAILED }
    @Id private UUID id;
    @Column(name="order_number",nullable=false,unique=true,length=64) private String orderNumber;
    @Column(name="idempotency_key",nullable=false,unique=true,length=128) private String idempotencyKey;
    @Column(name="customer_id",nullable=false,length=128) private String customerId;
    @Column(name="cart_id",nullable=false) private UUID cartId;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=32) private Status status;
    @Column(name="total_amount",nullable=false) private long totalAmount;
    @Column(name="voucher_amount",nullable=false) private long voucherAmount;
    @Column(name="payable_amount",nullable=false) private long payableAmount;
    @Column(nullable=false,length=3) private String currency;
    @Column(name="payment_id") private UUID paymentId;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    @Version @Column(nullable=false) private long version;
    @OneToMany(mappedBy="order",cascade=CascadeType.ALL,orphanRemoval=true,fetch=FetchType.EAGER) private List<OrderLine> lines=new ArrayList<>();
    protected SalesOrder(){}
    public static SalesOrder create(ShoppingCart cart,String key,long voucherAmount){SalesOrder o=new SalesOrder();o.id=UUID.randomUUID();o.orderNumber="ORD-"+o.id.toString().substring(0,8).toUpperCase();o.idempotencyKey=key;o.customerId=cart.customerId();o.cartId=cart.id();o.totalAmount=cart.total();o.voucherAmount=voucherAmount;o.payableAmount=o.totalAmount-voucherAmount;o.currency="CNY";o.status=o.payableAmount==0?Status.PAID:Status.PENDING_PAYMENT;o.createdAt=Instant.now();o.updatedAt=o.createdAt;cart.items().forEach(i->o.lines.add(OrderLine.of(o,i)));return o;}
    public void attachPayment(UUID paymentId){if(status!=Status.PENDING_PAYMENT)throw new IllegalStateException("Order does not need external payment");this.paymentId=paymentId;updatedAt=Instant.now();}
    public void paid(){if(status==Status.PAID)return;if(status!=Status.PENDING_PAYMENT)throw new IllegalStateException("Order cannot be paid");status=Status.PAID;updatedAt=Instant.now();}
    public void paymentFailed(){if(status!=Status.PENDING_PAYMENT)throw new IllegalStateException("Order is not awaiting payment");status=Status.PAYMENT_FAILED;updatedAt=Instant.now();}
    public UUID id(){return id;} public String orderNumber(){return orderNumber;} public String customerId(){return customerId;} public UUID cartId(){return cartId;} public Status status(){return status;} public long totalAmount(){return totalAmount;} public long voucherAmount(){return voucherAmount;} public long payableAmount(){return payableAmount;} public String currency(){return currency;} public UUID paymentId(){return paymentId;} public List<OrderLine> lines(){return List.copyOf(lines);} public Instant createdAt(){return createdAt;}
}
