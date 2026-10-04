package com.xin.payment.commerce;

import jakarta.persistence.*;

@Entity
@Table(name="inventory_item")
public class InventoryItem {
    @Id @Column(length=64) private String sku;
    @Column(name="on_hand",nullable=false) private long onHand;
    @Column(nullable=false) private long reserved;
    @Version @Column(nullable=false) private long version;
    protected InventoryItem() {}
    public void reserve(int quantity){if(quantity<=0||available()<quantity)throw new IllegalStateException("Insufficient stock for "+sku);reserved+=quantity;}
    public void release(int quantity){if(quantity<=0||reserved<quantity)throw new IllegalStateException("Invalid stock release for "+sku);reserved-=quantity;}
    public void commit(int quantity){if(quantity<=0||reserved<quantity||onHand<quantity)throw new IllegalStateException("Invalid stock commit for "+sku);reserved-=quantity;onHand-=quantity;}
    public String sku(){return sku;} public long onHand(){return onHand;} public long reserved(){return reserved;} public long available(){return onHand-reserved;}
}
