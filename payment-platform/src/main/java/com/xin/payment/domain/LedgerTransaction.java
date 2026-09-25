package com.xin.payment.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ledger_transaction")
public class LedgerTransaction {
    @Id private UUID id;
    @Column(name = "reference_type", nullable = false, length = 32) private String referenceType;
    @Column(name = "reference_id", nullable = false) private UUID referenceId;
    @Column(nullable = false, length = 256) private String description;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("createdAt ASC")
    private List<LedgerEntry> entries = new ArrayList<>();

    protected LedgerTransaction() {}

    public static LedgerTransaction create(String type, UUID referenceId, String description) {
        LedgerTransaction transaction = new LedgerTransaction();
        transaction.id = UUID.randomUUID();
        transaction.referenceType = type;
        transaction.referenceId = referenceId;
        transaction.description = description;
        transaction.createdAt = Instant.now();
        return transaction;
    }

    public LedgerTransaction debit(String account, long amount, String currency) { entries.add(LedgerEntry.of(this, account, LedgerEntry.Side.DEBIT, amount, currency)); return this; }
    public LedgerTransaction credit(String account, long amount, String currency) { entries.add(LedgerEntry.of(this, account, LedgerEntry.Side.CREDIT, amount, currency)); return this; }
    public void requireBalanced() {
        long debits = entries.stream().filter(e -> e.side() == LedgerEntry.Side.DEBIT).mapToLong(LedgerEntry::amount).sum();
        long credits = entries.stream().filter(e -> e.side() == LedgerEntry.Side.CREDIT).mapToLong(LedgerEntry::amount).sum();
        if (debits != credits) throw new IllegalStateException("Ledger transaction is not balanced");
    }
    public UUID id() { return id; }
    public String referenceType() { return referenceType; }
    public UUID referenceId() { return referenceId; }
    public String description() { return description; }
    public Instant createdAt() { return createdAt; }
    public List<LedgerEntry> entries() { return List.copyOf(entries); }
}
