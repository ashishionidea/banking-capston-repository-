package com.ashish.banking.accountservice.service;
import com.ashish.banking.accountservice.domain.BankAccount;import com.ashish.banking.accountservice.domain.AccountTransaction;import com.ashish.banking.accountservice.repository.BankAccountRepository;import com.ashish.banking.accountservice.repository.AccountTransactionRepository;
import org.springframework.kafka.annotation.KafkaListener;import org.springframework.kafka.core.KafkaTemplate;import org.springframework.stereotype.Component;import org.springframework.transaction.annotation.Transactional;import java.math.BigDecimal;import java.time.Instant;
@Component public class AccountTransferListener{
 public record TransferEvent(String reference,Long customerId,String sourceAccountNumber,String destinationAccountNumber,BigDecimal amount,String currency,String reason){}
 private final BankAccountRepository accounts;private final AccountTransactionRepository ledger;private final KafkaTemplate<String,Object> kafka;
 public AccountTransferListener(BankAccountRepository a,AccountTransactionRepository l,KafkaTemplate<String,Object> k){accounts=a;ledger=l;kafka=k;}
 @KafkaListener(topics="banking.transfer.commands")
 @Transactional public void debit(TransferEvent e){
  if(ledger.existsByReferenceAndAccountNumber(e.reference(),e.sourceAccountNumber())){kafka.send("banking.transfer.source-debited",e.reference(),e).join();return;}
  BankAccount s=accounts.findLocked(e.sourceAccountNumber()).orElseThrow(()->new IllegalArgumentException("Source account not found"));
  BankAccount d=accounts.findByAccountNumber(e.destinationAccountNumber()).orElseThrow(()->new IllegalArgumentException("Destination account not found"));
  if(!s.getCustomerId().equals(e.customerId()))throw new IllegalArgumentException("Source account does not belong to caller");
  if(!"ACTIVE".equals(s.getStatus())||!"ACTIVE".equals(d.getStatus()))throw new IllegalStateException("Account is not active");
  if(!s.getCurrency().equals(e.currency())||!d.getCurrency().equals(e.currency()))throw new IllegalArgumentException("Currency mismatch");
  if(s.getBalance().compareTo(e.amount())<0){kafka.send("banking.transfer.debit-rejected",e.reference(),new TransferEvent(e.reference(),e.customerId(),e.sourceAccountNumber(),e.destinationAccountNumber(),e.amount(),e.currency(),"Insufficient funds")).join();return;}
  s.debit(e.amount());accounts.save(s);
  writeLedger(e.reference(),s.getAccountNumber(),"TRANSFER_DEBIT","COMPLETED",e.amount().negate(),e.currency());
  kafka.send("banking.transfer.source-debited",e.reference(),e).join();
 }
 @KafkaListener(topics="banking.transfer.credit-destination")
 @Transactional public void credit(TransferEvent e){
  if(ledger.existsByReferenceAndAccountNumber(e.reference(),e.destinationAccountNumber())){kafka.send("banking.transfer.destination-credited",e.reference(),e).join();return;}
  BankAccount d=accounts.findLocked(e.destinationAccountNumber()).orElseThrow(()->new IllegalArgumentException("Destination account not found"));
  if(!"ACTIVE".equals(d.getStatus())){kafka.send("banking.transfer.destination-rejected",e.reference(),new TransferEvent(e.reference(),e.customerId(),e.sourceAccountNumber(),e.destinationAccountNumber(),e.amount(),e.currency(),"Destination account inactive")).join();return;}
  d.credit(e.amount());accounts.save(d);writeLedger(e.reference(),d.getAccountNumber(),"TRANSFER_CREDIT","COMPLETED",e.amount(),e.currency());
  kafka.send("banking.transfer.destination-credited",e.reference(),e).join();
 }
 @KafkaListener(topics="banking.transfer.compensate-source")
 @Transactional public void compensate(TransferEvent e){
  if(ledger.existsByReferenceAndAccountNumber("COMP-"+e.reference(),e.sourceAccountNumber())){kafka.send("banking.transfer.source-compensated",e.reference(),e).join();return;}
  BankAccount s=accounts.findLocked(e.sourceAccountNumber()).orElseThrow(()->new IllegalArgumentException("Source account not found"));
  s.credit(e.amount());accounts.save(s);writeLedger("COMP-"+e.reference(),s.getAccountNumber(),"TRANSFER_COMPENSATION","COMPLETED",e.amount(),e.currency());
  kafka.send("banking.transfer.source-compensated",e.reference(),new TransferEvent(e.reference(),e.customerId(),e.sourceAccountNumber(),e.destinationAccountNumber(),e.amount(),e.currency(),"Compensating source credit applied")).join();
 }
 private void writeLedger(String ref,String acct,String type,String status,BigDecimal amount,String currency){
  AccountTransaction row=new AccountTransaction();row.setReference(ref);row.setAccountNumber(acct);row.setType(type);row.setStatus(status);row.setAmount(amount);row.setCurrency(currency);row.setCreatedAt(Instant.now());ledger.save(row);
 }
}
