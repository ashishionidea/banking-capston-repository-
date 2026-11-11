package com.ashish.banking.accountservice.repository;
import com.ashish.banking.accountservice.domain.BankAccount; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.util.*;
public interface BankAccountRepository extends JpaRepository<BankAccount,Long>{
 List<BankAccount> findByCustomerId(Long customerId); Optional<BankAccount> findByAccountNumber(String accountNumber);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select a from BankAccount a where a.accountNumber=:number") Optional<BankAccount> findLocked(@Param("number") String number);
}
