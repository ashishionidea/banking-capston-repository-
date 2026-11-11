package com.ashish.banking.accountservice.api;
import com.ashish.banking.accountservice.domain.BankAccount; import com.ashish.banking.accountservice.repository.BankAccountRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal; import org.springframework.security.oauth2.jwt.Jwt; import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.util.*;
@RestController @RequestMapping("/api/accounts")
public class AccountController {
 private final BankAccountRepository accounts; public AccountController(BankAccountRepository a){accounts=a;}
 public record AccountView(String accountNumber,BigDecimal balance,String currency,String status){}
 @GetMapping @Transactional(readOnly=true) public List<AccountView> list(@AuthenticationPrincipal Jwt jwt){return accounts.findByCustomerId(customer(jwt)).stream().map(this::view).toList();}
 @GetMapping("/{accountNumber}") @Transactional(readOnly=true) public AccountView get(@PathVariable String accountNumber,@AuthenticationPrincipal Jwt jwt){return view(owned(accountNumber,customer(jwt)));}
 @GetMapping("/{accountNumber}/balance") @Transactional(readOnly=true) public AccountView balance(@PathVariable String accountNumber,@AuthenticationPrincipal Jwt jwt){return view(owned(accountNumber,customer(jwt)));}
 private BankAccount owned(String n,Long c){BankAccount a=accounts.findByAccountNumber(n).orElseThrow(()->new IllegalArgumentException("Account not found"));if(!a.getCustomerId().equals(c))throw new IllegalArgumentException("Account does not belong to customer");return a;}
 private Long customer(Jwt j){return Long.valueOf(j.getSubject());}
 private AccountView view(BankAccount a){return new AccountView(a.getAccountNumber(),a.getBalance(),a.getCurrency(),a.getStatus());}
}
