package com.ashish.banking.statementservice.api;
import com.ashish.banking.statementservice.client.AccountTransactionsClient;import jakarta.validation.constraints.NotBlank;import org.springframework.format.annotation.DateTimeFormat;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;import org.springframework.web.bind.annotation.*;import org.springframework.http.*;import java.time.*;import java.util.*;
@RestController @RequestMapping("/api/statements")
public class StatementController{
 private final AccountTransactionsClient client;public StatementController(AccountTransactionsClient c){client=c;}
 @GetMapping(produces="text/csv") public ResponseEntity<String> csv(@RequestParam @NotBlank String accountNumber,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,@AuthenticationPrincipal Jwt jwt){
  if(to.isBefore(from))throw new IllegalArgumentException("End date must not precede start date");
  List<AccountTransactionsClient.TransactionItem> rows=client.fetch(accountNumber,jwt.getTokenValue()).stream().filter(t->!t.createdAt().isBefore(from.atStartOfDay(ZoneOffset.UTC).toInstant())&&t.createdAt().isBefore(to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant())).toList();
  StringBuilder b=new StringBuilder("reference,type,status,amount,currency,createdAt\n");
  for(var t:rows)b.append(csv(t.reference())).append(',').append(csv(t.type())).append(',').append(csv(t.status())).append(',').append(t.amount()).append(',').append(csv(t.currency())).append(',').append(t.createdAt()).append('\n');
  return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=account-statement.csv").contentType(MediaType.parseMediaType("text/csv")).body(b.toString());
 }
 private String csv(String value){if(value==null)return "";return "\""+value.replace("\"","\\"")+"\"";}
}
