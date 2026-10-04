package com.xin.payment.commerce;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name="order_line")
public class OrderLine {
    @Id private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="order_id") private SalesOrder order;
    @Column(nullable=false,length=64) private String sku;
    @Column(name="product_name",nullable=false,length=128) private String productName;
    @Column(name="unit_price",nullable=false) private long unitPrice;
    @Column(nullable=false) private int quantity;
    @Column(name="line_total",nullable=false) private long lineTotal;
    protected OrderLine(){}
    static OrderLine of(SalesOrder order,CartItem item){OrderLine l=new OrderLine();l.id=UUID.randomUUID();l.order=order;l.sku=item.sku();l.productName=item.productName();l.unitPrice=item.unitPrice();l.quantity=item.quantity();l.lineTotal=item.lineTotal();return l;}
    public String sku(){return sku;} public String productName(){return productName;} public long unitPrice(){return unitPrice;} public int quantity(){return quantity;} public long lineTotal(){return lineTotal;}
}
