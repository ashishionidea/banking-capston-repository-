package com.ashish.banking.statementservice.client;
import org.springframework.beans.factory.annotation.Value;import org.springframework.http.MediaType;import org.springframework.stereotype.Component;import org.springframework.web.client.RestClient;import java.time.Instant;import java.util.List;
@Component public class AccountTransactionsClient{
 private final RestClient client;
 public AccountTransactionsClient(@Value("${ACCOUNT_SERVICE_URL:http://localhost:8082}")String base){client=RestClient.builder().baseUrl(base).build();}
 public record TransactionItem(String reference,String type,String status,java.math.BigDecimal amount,String currency,Instant createdAt){}
 public List<TransactionItem> fetch(String account,String token){TransactionItem[] items=client.get().uri("/internal/accounts/{account}/transactions",account).header("Authorization","Bearer "+token).accept(MediaType.APPLICATION_JSON).retrieve().body(TransactionItem[].class);return items==null?List.of():List.of(items);}
}
