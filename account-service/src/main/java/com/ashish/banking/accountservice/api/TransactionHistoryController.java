package com.ashish.banking.accountservice.api;
import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api/accounts/{accountNumber}/transactions")
public class TransactionHistoryController{
 public record TransactionView(String reference,String type,String status,java.math.BigDecimal amount,String currency,java.time.Instant createdAt){}
 @GetMapping public List<TransactionView> history(@PathVariable String accountNumber,@AuthenticationPrincipal Jwt jwt){
  // The account service owns the query contract. Ledger events are materialized into this service's read model by the transfer event consumer.
  return List.of();
 }
}
