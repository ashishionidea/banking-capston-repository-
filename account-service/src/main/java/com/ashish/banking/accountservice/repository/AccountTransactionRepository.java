package com.ashish.banking.accountservice.repository;
import com.ashish.banking.accountservice.domain.AccountTransaction;import org.springframework.data.jpa.repository.JpaRepository;import java.util.List;
public interface AccountTransactionRepository extends JpaRepository<AccountTransaction,Long>{List<AccountTransaction> findByAccountNumberOrderByCreatedAtDesc(String accountNumber);boolean existsByReferenceAndAccountNumber(String reference,String accountNumber);}
