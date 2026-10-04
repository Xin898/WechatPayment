package com.xin.payment.commerce;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class InventoryService {
    private final InventoryRepository inventory;
    public InventoryService(InventoryRepository inventory){this.inventory=inventory;}
    public void reserve(List<CartItem> lines){lines.forEach(line->item(line.sku()).reserve(line.quantity()));}
    public void commit(List<OrderLine> lines){lines.forEach(line->item(line.sku()).commit(line.quantity()));}
    public void release(List<OrderLine> lines){lines.forEach(line->item(line.sku()).release(line.quantity()));}
    public InventoryItem item(String sku){return inventory.findById(sku).orElseThrow(()->new IllegalArgumentException("Inventory not found: "+sku));}
    public List<InventoryItem> all(){return inventory.findAll();}
}
