package com.ashish.banking.transferservice.service;
import com.ashish.banking.transferservice.domain.TransferSaga;import com.ashish.banking.transferservice.messaging.TransferEvent;import com.ashish.banking.transferservice.repository.TransferSagaRepository;
import org.springframework.beans.factory.annotation.Value;import org.springframework.kafka.core.KafkaTemplate;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import org.springframework.transaction.support.*;import java.math.BigDecimal;import java.util.UUID;
@Service public class TransferService{
 private final TransferSagaRepository sagas;private final KafkaTemplate<String,Object> kafka;private final String commands;
 public TransferService(TransferSagaRepository s,KafkaTemplate<String,Object> k,@Value("${app.kafka.topics.transfer-commands:banking.transfer.commands}")String c){sagas=s;kafka=k;commands=c;}
 @Transactional public TransferSaga start(String key,Long customer,String source,String destination,BigDecimal amount,String currency){
  if(key==null||key.isBlank())throw new IllegalArgumentException("Idempotency-Key required");if(amount==null||amount.signum()<=0)throw new IllegalArgumentException("Amount must be positive");
  TransferSaga old=sagas.findByIdempotencyKey(key).orElse(null);
  if(old!=null){if(!old.getCustomerId().equals(customer)||!old.getSourceAccountNumber().equals(source)||!old.getDestinationAccountNumber().equals(destination)||old.getAmount().compareTo(amount)!=0||!old.getCurrency().equals(currency))throw new IllegalStateException("Idempotency-Key reused with different request");if("STARTED".equals(old.getStatus()))publishAfterCommit(old);return old;}
  if(source.equals(destination))throw new IllegalArgumentException("Source and destination must differ");
  TransferSaga s=new TransferSaga();s.setReference("TR-"+UUID.randomUUID());s.setIdempotencyKey(key);s.setCustomerId(customer);s.setSourceAccountNumber(source);s.setDestinationAccountNumber(destination);s.setAmount(amount.setScale(2,java.math.RoundingMode.UNNECESSARY));s.setCurrency(currency);s.setStatus("STARTED");s=sagas.save(s);publishAfterCommit(s);return s;
 }
 private void publishAfterCommit(TransferSaga s){if(TransactionSynchronizationManager.isSynchronizationActive())TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){public void afterCommit(){publish(s);}});else publish(s);}
 private void publish(TransferSaga s){kafka.send(commands,s.getReference(),new TransferEvent(s.getReference(),s.getCustomerId(),s.getSourceAccountNumber(),s.getDestinationAccountNumber(),s.getAmount(),s.getCurrency(),null)).join();}
 @Transactional(readOnly=true) public TransferSaga get(String ref,Long customer){TransferSaga s=sagas.findByReference(ref).orElseThrow(()->new IllegalArgumentException("Transfer not found"));if(!s.getCustomerId().equals(customer))throw new IllegalArgumentException("Transfer not owned by customer");return s;}
}
