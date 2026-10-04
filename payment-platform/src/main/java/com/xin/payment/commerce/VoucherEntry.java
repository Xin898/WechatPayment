package com.xin.payment.commerce;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="voucher_entry")
public class VoucherEntry {
    public enum Type { ISSUE, SPEND, REFUND }
    @Id private UUID id;
    @Column(name="customer_id",nullable=false,length=128) private String customerId;
    @Enumerated(EnumType.STRING) @Column(name="entry_type",nullable=false,length=16) private Type type;
    @Column(nullable=false) private long amount;
    @Column(name="entry_reference",nullable=false,length=128) private String reference;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    protected VoucherEntry(){}
    public static VoucherEntry of(String customerId,Type type,long amount,String reference){VoucherEntry e=new VoucherEntry();e.id=UUID.randomUUID();e.customerId=customerId;e.type=type;e.amount=amount;e.reference=reference;e.createdAt=Instant.now();return e;}
    public UUID id(){return id;} public Type type(){return type;} public long amount(){return amount;} public String reference(){return reference;} public Instant createdAt(){return createdAt;}
}
