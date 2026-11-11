package com.ashish.banking.notificationservice.repository;
import com.ashish.banking.notificationservice.domain.NotificationRecord;import org.springframework.data.jpa.repository.JpaRepository;import java.util.Optional;
public interface NotificationRecordRepository extends JpaRepository<NotificationRecord,Long>{Optional<NotificationRecord> findByEventId(String eventId);}
