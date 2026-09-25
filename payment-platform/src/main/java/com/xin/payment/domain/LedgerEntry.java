package com.xin.payment.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "ledger_entry")
public class LedgerEntry {
    public enum Side { DEBIT, CREDIT }
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "transaction_id") private LedgerTransaction transaction;
    @Column(name = "account_code", nullable = false, length = 96) private String accountCode;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 8) private Side side;
    @Column(nullable = false) private long amount;
    @Column(nullable = false, length = 3) private String currency;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected LedgerEntry() {}
    static LedgerEntry of(LedgerTransaction transaction, String account, Side side, long amount, String currency) {
        if (amount <= 0) throw new IllegalArgumentException("Ledger amount must be positive");
        LedgerEntry entry = new LedgerEntry();
        entry.id = UUID.randomUUID(); entry.transaction = transaction; entry.accountCode = account;
        entry.side = side; entry.amount = amount; entry.currency = currency.toUpperCase(Locale.ROOT); entry.createdAt = Instant.now();
        return entry;
    }
    public UUID id() { return id; }
    public String accountCode() { return accountCode; }
    public Side side() { return side; }
    public long amount() { return amount; }
    public String currency() { return currency; }
}
