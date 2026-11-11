package com.ashish.banking.accountservice.api;
import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/accounts/{accountNumber}/transactions")
public class TransactionHistoryController {
 public record TransactionView(String reference,String type,String status,java.math.BigDecimal amount,String currency,java.time.Instant createdAt){}
 @GetMapping public List<TransactionView> history(@PathVariable String accountNumber){return List.of();}
}
