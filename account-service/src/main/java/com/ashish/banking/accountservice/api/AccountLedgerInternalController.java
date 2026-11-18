package com.ashish.banking.accountservice.api;
import com.ashish.banking.accountservice.domain.AccountTransaction;import com.ashish.banking.accountservice.repository.AccountTransactionRepository;import jakarta.validation.Valid;import jakarta.validation.constraints.*;import org.springframework.http.HttpStatus;import org.springframework.web.bind.annotation.*;import java.math.BigDecimal;import java.time.Instant;import java.util.List;
@RestController
public class AccountLedgerInternalController{
 private final AccountTransactionRepository transactions;public AccountLedgerInternalController(AccountTransactionRepository t){transactions=t;}
 public record LedgerEntry(@NotBlank String reference,@NotBlank String accountNumber,@NotBlank String type,@NotBlank String status,@NotNull BigDecimal amount,@NotBlank String currency,Instant createdAt){}
 @PostMapping("/internal/ledger") @ResponseStatus(HttpStatus.CREATED) public void add(@Valid@RequestBody LedgerEntry r){save(r);}
 @GetMapping("/internal/accounts/{accountNumber}/transactions") public List<LedgerEntry> get(@PathVariable String accountNumber){return transactions.findByAccountNumberOrderByCreatedAtDesc(accountNumber).stream().map(t->new LedgerEntry(t.getReference(),t.getAccountNumber(),t.getType(),t.getStatus(),t.getAmount(),t.getCurrency(),t.getCreatedAt())).toList();}
 private void save(LedgerEntry r){if(transactions.existsByReferenceAndAccountNumber(r.reference(),r.accountNumber()))return;AccountTransaction t=new AccountTransaction();t.setReference(r.reference());t.setAccountNumber(r.accountNumber());t.setType(r.type());t.setStatus(r.status());t.setAmount(r.amount());t.setCurrency(r.currency());t.setCreatedAt(r.createdAt()==null?Instant.now():r.createdAt());transactions.save(t);}
}
