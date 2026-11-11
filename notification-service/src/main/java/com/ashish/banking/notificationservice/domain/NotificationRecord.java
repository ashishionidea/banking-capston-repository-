package com.ashish.banking.notificationservice.domain;
import jakarta.persistence.*;import java.time.Instant;
@Entity @Table(name="notification_records") public class NotificationRecord{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;
 @Column(nullable=false,unique=true)private String eventId;
 @Column(nullable=false)private String channel;
 @Column(nullable=false)private String recipientMasked;
 @Column(nullable=false,length=2000)private String message;
 @Column(nullable=false)private String status="ACCEPTED";
 @Column(nullable=false)private Instant createdAt=Instant.now();
 public Long getId(){return id;}public String getEventId(){return eventId;}public void setEventId(String v){eventId=v;}
 public String getChannel(){return channel;}public void setChannel(String v){channel=v;}
 public String getRecipientMasked(){return recipientMasked;}public void setRecipientMasked(String v){recipientMasked=v;}
 public String getMessage(){return message;}public void setMessage(String v){message=v;}
 public String getStatus(){return status;}public void setStatus(String v){status=v;}public Instant getCreatedAt(){return createdAt;}
}
