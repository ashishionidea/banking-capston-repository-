package com.ashish.banking.transferservice.messaging;
import com.ashish.banking.transferservice.domain.TransferSaga;import com.ashish.banking.transferservice.repository.TransferSagaRepository;import org.springframework.kafka.annotation.KafkaListener;import org.springframework.kafka.core.KafkaTemplate;import org.springframework.stereotype.Component;import org.springframework.transaction.annotation.Transactional;import java.util.Optional;
/** Starter coordinator: the account service consumes debit/credit commands and emits outcomes. */
@Component public class TransferSagaListener{
 private final TransferSagaRepository sagas;private final KafkaTemplate<String,Object> kafka;
 public TransferSagaListener(TransferSagaRepository s,KafkaTemplate<String,Object> k){sagas=s;kafka=k;}
 @KafkaListener(topics="${app.kafka.topics.source-debited:banking.transfer.source-debited}") @Transactional
 public void sourceDebited(TransferEvent e){TransferSaga s=sagas.findByReference(e.reference()).orElseThrow();if("COMPLETED".equals(s.getStatus())||"COMPENSATED".equals(s.getStatus()))return;s.setStatus("DEBITED");sagas.save(s);kafka.send("banking.transfer.credit-destination",e.reference(),e).join();}
 @KafkaListener(topics="${app.kafka.topics.destination-credited:banking.transfer.destination-credited}") @Transactional
 public void destinationCredited(TransferEvent e){TransferSaga s=sagas.findByReference(e.reference()).orElseThrow();if("COMPLETED".equals(s.getStatus()))return;s.setStatus("COMPLETED");sagas.save(s);kafka.send("banking.transfer.completed",e.reference(),e).join();}
 @KafkaListener(topics="${app.kafka.topics.destination-rejected:banking.transfer.destination-rejected}") @Transactional
 public void destinationRejected(TransferEvent e){TransferSaga s=sagas.findByReference(e.reference()).orElseThrow();if("COMPENSATED".equals(s.getStatus()))return;kafka.send("banking.transfer.compensate-source",e.reference(),e).join();}
 @KafkaListener(topics="${app.kafka.topics.source-compensated:banking.transfer.source-compensated}") @Transactional
 public void sourceCompensated(TransferEvent e){TransferSaga s=sagas.findByReference(e.reference()).orElseThrow();s.setStatus("COMPENSATED");s.setFailureReason(e.reason());sagas.save(s);}
}
