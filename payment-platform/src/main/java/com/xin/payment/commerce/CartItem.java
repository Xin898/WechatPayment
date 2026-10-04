package com.xin.payment.commerce;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name="cart_item")
public class CartItem {
    @Id private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="cart_id") private ShoppingCart cart;
    @Column(nullable=false,length=64) private String sku;
    @Column(name="product_name",nullable=false,length=128) private String productName;
    @Column(name="unit_price",nullable=false) private long unitPrice;
    @Column(nullable=false) private int quantity;
    protected CartItem(){}
    static CartItem of(ShoppingCart cart,CatalogProduct product,int quantity){CartItem i=new CartItem();i.id=UUID.randomUUID();i.cart=cart;i.sku=product.sku();i.productName=product.name();i.unitPrice=product.unitPrice();i.quantity=quantity;return i;}
    void increase(int quantity){this.quantity+=quantity;}
    public String sku(){return sku;} public String productName(){return productName;} public long unitPrice(){return unitPrice;} public int quantity(){return quantity;} public long lineTotal(){return unitPrice*quantity;}
}
