package com.ashish.banking.billpaymentservice.repository;
import com.ashish.banking.billpaymentservice.domain.BillPayment;import org.springframework.data.jpa.repository.JpaRepository;import java.util.Optional;
public interface BillPaymentRepository extends JpaRepository<BillPayment,Long>{Optional<BillPayment> findByReferenceAndCustomerId(String reference,Long customerId);}
