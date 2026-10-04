package com.xin.payment.commerce;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class CartService {
    private final CartRepository carts; private final ProductRepository products;
    public CartService(CartRepository carts,ProductRepository products){this.carts=carts;this.products=products;}
    @Transactional public ShoppingCart create(String customerId){if(customerId==null||customerId.isBlank())throw new IllegalArgumentException("customerId is required");return carts.save(ShoppingCart.open(customerId));}
    @Transactional public ShoppingCart add(UUID cartId,String sku,int quantity){ShoppingCart cart=get(cartId);CatalogProduct product=products.findById(sku).filter(CatalogProduct::active).orElseThrow(()->new IllegalArgumentException("Unknown product: "+sku));cart.add(product,quantity);return cart;}
    @Transactional(readOnly=true) public ShoppingCart get(UUID id){return carts.findById(id).orElseThrow(()->new IllegalArgumentException("Cart not found: "+id));}
    @Transactional(readOnly=true) public List<CatalogProduct> products(){return products.findByActiveTrueOrderBySku();}
}
