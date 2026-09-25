package com.xin.payment.infrastructure;

import com.xin.payment.domain.LedgerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface LedgerTransactionRepository extends JpaRepository<LedgerTransaction, UUID> {
    List<LedgerTransaction> findByReferenceIdOrderByCreatedAtDesc(UUID referenceId);
}
