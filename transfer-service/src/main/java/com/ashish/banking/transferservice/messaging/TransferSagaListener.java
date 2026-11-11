package com.ashish.banking.transferservice.messaging;
import com.ashish.banking.transferservice.domain.TransferSaga;import com.ashish.banking.transferservice.repository.TransferSagaRepository;import org.springframework.kafka.annotation.KafkaListener;import org.springframework.kafka.core.KafkaTemplate;import org.springframework.stereotype.Component;import org.springframework.transaction.annotation.Transactional;
/** Transfer Saga coordinator. Each transition is idempotent and keyed by the transfer reference. */
@Component public class TransferSagaListener{
 private final TransferSagaRepository sagas;private final KafkaTemplate<String,Object> kafka;
 public TransferSagaListener(TransferSagaRepository s,KafkaTemplate<String,Object> k){sagas=s;kafka=k;}
 @KafkaListener(topics="${app.kafka.topics.source-debited:banking.transfer.source-debited}")
 @Transactional public void sourceDebited(TransferEvent e){TransferSaga s=sagas.findByReference(e.reference()).orElseThrow();if("COMPLETED".equals(s.getStatus())||"COMPENSATED".equals(s.getStatus()))return;if(!"STARTED".equals(s.getStatus())&&!"DEBITED".equals(s.getStatus()))return;s.setStatus("DEBITED");sagas.save(s);kafka.send("banking.transfer.credit-destination",e.reference(),e).join();}
 @KafkaListener(topics="${app.kafka.topics.destination-credited:banking.transfer.destination-credited}")
 @Transactional public void destinationCredited(TransferEvent e){TransferSaga s=sagas.findByReference(e.reference()).orElseThrow();if("COMPLETED".equals(s.getStatus()))return;if(!"DEBITED".equals(s.getStatus()))return;s.setStatus("COMPLETED");sagas.save(s);kafka.send("banking.transfer.completed",e.reference(),e).join();}
 @KafkaListener(topics="${app.kafka.topics.debit-rejected:banking.transfer.debit-rejected}")
 @Transactional public void debitRejected(TransferEvent e){TransferSaga s=sagas.findByReference(e.reference()).orElseThrow();if("STARTED".equals(s.getStatus())){s.setStatus("FAILED");s.setFailureReason(e.reason());sagas.save(s);}}
 @KafkaListener(topics="${app.kafka.topics.destination-rejected:banking.transfer.destination-rejected}")
 @Transactional public void destinationRejected(TransferEvent e){TransferSaga s=sagas.findByReference(e.reference()).orElseThrow();if("COMPENSATED".equals(s.getStatus())||"COMPENSATION_FAILED".equals(s.getStatus()))return;if("DEBITED".equals(s.getStatus()))kafka.send("banking.transfer.compensate-source",e.reference(),e).join();}
 @KafkaListener(topics="${app.kafka.topics.source-compensated:banking.transfer.source-compensated}")
 @Transactional public void sourceCompensated(TransferEvent e){TransferSaga s=sagas.findByReference(e.reference()).orElseThrow();if("COMPENSATED".equals(s.getStatus()))return;if("DEBITED".equals(s.getStatus())){s.setStatus("COMPENSATED");s.setFailureReason(e.reason());sagas.save(s);}}
}
