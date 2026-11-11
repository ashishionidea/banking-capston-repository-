package com.ashish.banking.loanservice.repository;
import com.ashish.banking.loanservice.domain.LoanApplication;import org.springframework.data.jpa.repository.JpaRepository;import java.util.Optional;
public interface LoanApplicationRepository extends JpaRepository<LoanApplication,Long>{Optional<LoanApplication> findByReferenceAndCustomerId(String reference,Long customerId);}
