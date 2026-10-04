package com.xin.payment.commerce;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name="shopping_cart")
public class ShoppingCart {
    public enum Status { OPEN, CHECKED_OUT }
    @Id private UUID id;
    @Column(name="customer_id",nullable=false,length=128) private String customerId;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=24) private Status status;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    @Version @Column(nullable=false) private long version;
    @OneToMany(mappedBy="cart",cascade=CascadeType.ALL,orphanRemoval=true,fetch=FetchType.EAGER) private List<CartItem> items=new ArrayList<>();
    protected ShoppingCart(){}
    public static ShoppingCart open(String customerId){ShoppingCart c=new ShoppingCart();c.id=UUID.randomUUID();c.customerId=Objects.requireNonNull(customerId);c.status=Status.OPEN;c.createdAt=Instant.now();c.updatedAt=c.createdAt;return c;}
    public void add(CatalogProduct product,int quantity){requireOpen();if(quantity<=0)throw new IllegalArgumentException("quantity must be positive");items.stream().filter(i->i.sku().equals(product.sku())).findFirst().ifPresentOrElse(i->i.increase(quantity),()->items.add(CartItem.of(this,product,quantity)));updatedAt=Instant.now();}
    public void checkout(){requireOpen();if(items.isEmpty())throw new IllegalStateException("Cart is empty");status=Status.CHECKED_OUT;updatedAt=Instant.now();}
    private void requireOpen(){if(status!=Status.OPEN)throw new IllegalStateException("Cart is not open");}
    public UUID id(){return id;} public String customerId(){return customerId;} public Status status(){return status;} public List<CartItem> items(){return List.copyOf(items);}
    public long total(){return items.stream().mapToLong(CartItem::lineTotal).sum();}
}
