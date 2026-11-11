package com.ashish.banking.supportservice.repository;
import com.ashish.banking.supportservice.domain.SupportTicket;import org.springframework.data.jpa.repository.JpaRepository;import java.util.Optional;
public interface SupportTicketRepository extends JpaRepository<SupportTicket,Long>{Optional<SupportTicket> findByReferenceAndCustomerId(String reference,Long customerId);}
