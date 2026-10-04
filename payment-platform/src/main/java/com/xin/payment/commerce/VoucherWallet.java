package com.xin.payment.commerce;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="voucher_wallet")
public class VoucherWallet {
    @Id @Column(name="customer_id",length=128) private String customerId;
    @Column(nullable=false) private long balance;
    @Column(nullable=false,length=3) private String currency;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    @Version @Column(nullable=false) private long version;
    protected VoucherWallet(){}
    public static VoucherWallet empty(String customerId,String currency){VoucherWallet w=new VoucherWallet();w.customerId=customerId;w.currency=currency;w.balance=0;w.updatedAt=Instant.now();return w;}
    public void issue(long amount){if(amount<=0)throw new IllegalArgumentException("Voucher amount must be positive");balance+=amount;updatedAt=Instant.now();}
    public void spend(long amount){if(amount<0||amount>balance)throw new IllegalStateException("Insufficient voucher balance");balance-=amount;updatedAt=Instant.now();}
    public void refund(long amount){if(amount<=0)throw new IllegalArgumentException("Refund amount must be positive");balance+=amount;updatedAt=Instant.now();}
    public String customerId(){return customerId;} public long balance(){return balance;} public String currency(){return currency;}
}
