package com.xin.payment.commerce;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

interface ProductRepository extends JpaRepository<CatalogProduct,String>{List<CatalogProduct> findByActiveTrueOrderBySku();}
interface InventoryRepository extends JpaRepository<InventoryItem,String>{}
interface CartRepository extends JpaRepository<ShoppingCart,UUID>{}
interface OrderRepository extends JpaRepository<SalesOrder,UUID>{Optional<SalesOrder> findByIdempotencyKey(String key);}
interface WalletRepository extends JpaRepository<VoucherWallet,String>{}
interface VoucherEntryRepository extends JpaRepository<VoucherEntry,UUID>{List<VoucherEntry> findByCustomerIdOrderByCreatedAtDesc(String customerId);}
