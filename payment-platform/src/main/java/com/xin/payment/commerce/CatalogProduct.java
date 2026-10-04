package com.xin.payment.commerce;

import jakarta.persistence.*;

@Entity
@Table(name="catalog_product")
public class CatalogProduct {
    @Id @Column(length=64) private String sku;
    @Column(nullable=false,length=128) private String name;
    @Column(name="unit_price",nullable=false) private long unitPrice;
    @Column(nullable=false,length=3) private String currency;
    @Column(nullable=false) private boolean active;
    protected CatalogProduct() {}
    public String sku(){return sku;} public String name(){return name;} public long unitPrice(){return unitPrice;}
    public String currency(){return currency;} public boolean active(){return active;}
}
