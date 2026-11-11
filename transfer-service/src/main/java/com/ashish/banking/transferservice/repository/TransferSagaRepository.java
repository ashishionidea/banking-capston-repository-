package com.ashish.banking.transferservice.repository;
import com.ashish.banking.transferservice.domain.TransferSaga;import org.springframework.data.jpa.repository.JpaRepository;import java.util.Optional;
public interface TransferSagaRepository extends JpaRepository<TransferSaga,Long>{Optional<TransferSaga> findByReference(String reference);Optional<TransferSaga> findByIdempotencyKey(String key);}
