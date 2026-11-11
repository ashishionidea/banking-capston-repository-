package com.ashish.banking.accountservice.api;
import com.ashish.banking.accountservice.domain.BankAccount; import com.ashish.banking.accountservice.repository.BankAccountRepository;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*; import java.math.BigDecimal; import java.util.UUID;
@RestController @RequestMapping("/internal/accounts")
public class AccountInternalController {
 private final BankAccountRepository accounts; public AccountInternalController(BankAccountRepository a){accounts=a;}
 public record OpenAccount(@NotNull Long customerId,@NotBlank String currency){}
 public record AccountCreated(String accountNumber,String status){}
 @PostMapping @Transactional public AccountCreated open(@Valid @RequestBody OpenAccount r){
  BankAccount a=new BankAccount();a.setCustomerId(r.customerId());a.setCurrency(r.currency().toUpperCase());a.setBalance(BigDecimal.ZERO);a.setAccountNumber("AC"+UUID.randomUUID().toString().replace("-","").substring(0,16).toUpperCase());return new AccountCreated(accounts.save(a).getAccountNumber(),a.getStatus());
 }
}
