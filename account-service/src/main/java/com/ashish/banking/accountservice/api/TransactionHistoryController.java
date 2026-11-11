package com.ashish.banking.accountservice.api;
import com.ashish.banking.accountservice.domain.AccountTransaction;import com.ashish.banking.accountservice.repository.AccountTransactionRepository;import com.ashish.banking.accountservice.repository.BankAccountRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;import org.springframework.transaction.annotation.Transactional;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api/accounts/{accountNumber}/transactions")
public class TransactionHistoryController{
 private final AccountTransactionRepository transactions;private final BankAccountRepository accounts;
 public TransactionHistoryController(AccountTransactionRepository t,BankAccountRepository a){transactions=t;accounts=a;}
 public record TransactionView(String reference,String type,String status,java.math.BigDecimal amount,String currency,java.time.Instant createdAt){}
 @GetMapping @Transactional(readOnly=true) public List<TransactionView> history(@PathVariable String accountNumber,@AuthenticationPrincipal Jwt jwt){
  var account=accounts.findByAccountNumber(accountNumber).orElseThrow(()->new IllegalArgumentException("Account not found"));
  if(!account.getCustomerId().equals(Long.valueOf(jwt.getSubject())))throw new IllegalArgumentException("Account does not belong to customer");
  return transactions.findByAccountNumberOrderByCreatedAtDesc(accountNumber).stream().map(t->new TransactionView(t.getReference(),t.getType(),t.getStatus(),t.getAmount(),t.getCurrency(),t.getCreatedAt())).toList();
 }
}
