package com.xin.payment.commerce;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class VoucherService {
    private final WalletRepository wallets; private final VoucherEntryRepository entries;
    public VoucherService(WalletRepository wallets,VoucherEntryRepository entries){this.wallets=wallets;this.entries=entries;}
    @Transactional public VoucherWallet issue(String customerId,long amount,String currency,String reference){VoucherWallet wallet=wallets.findById(customerId).orElseGet(()->VoucherWallet.empty(customerId,currency));if(!wallet.currency().equalsIgnoreCase(currency))throw new IllegalArgumentException("Wallet currency mismatch");wallet.issue(amount);wallets.save(wallet);entries.save(VoucherEntry.of(customerId,VoucherEntry.Type.ISSUE,amount,reference));return wallet;}
    public long available(String customerId){return wallets.findById(customerId).map(VoucherWallet::balance).orElse(0L);}
    public void spend(String customerId,long amount,String reference){if(amount==0)return;VoucherWallet wallet=wallets.findById(customerId).orElseThrow(()->new IllegalStateException("Voucher wallet not found"));wallet.spend(amount);entries.save(VoucherEntry.of(customerId,VoucherEntry.Type.SPEND,amount,reference));}
    public void refund(String customerId,long amount,String reference){if(amount==0)return;VoucherWallet wallet=wallets.findById(customerId).orElseThrow(()->new IllegalStateException("Voucher wallet not found"));wallet.refund(amount);entries.save(VoucherEntry.of(customerId,VoucherEntry.Type.REFUND,amount,reference));}
    @Transactional(readOnly=true) public VoucherWallet get(String customerId){return wallets.findById(customerId).orElseGet(()->VoucherWallet.empty(customerId,"CNY"));}
    @Transactional(readOnly=true) public List<VoucherEntry> entries(String customerId){return entries.findByCustomerIdOrderByCreatedAtDesc(customerId);}
}
