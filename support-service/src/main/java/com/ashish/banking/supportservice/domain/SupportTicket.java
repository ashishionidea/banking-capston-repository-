package com.ashish.banking.supportservice.domain;
import jakarta.persistence.*;import java.time.Instant;
@Entity @Table(name="support_tickets")
public class SupportTicket{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;
 @Column(nullable=false,unique=true)private String reference;
 @Column(nullable=false)private Long customerId;
 @Column(nullable=false,length=180)private String subject;
 @Column(nullable=false,length=4000)private String description;
 @Column(nullable=false,length=30)private String status="OPEN";
 @Column(nullable=false)private Instant createdAt=Instant.now();
 public Long getId(){return id;}public String getReference(){return reference;}public void setReference(String v){reference=v;}
 public Long getCustomerId(){return customerId;}public void setCustomerId(Long v){customerId=v;}
 public String getSubject(){return subject;}public void setSubject(String v){subject=v;}
 public String getDescription(){return description;}public void setDescription(String v){description=v;}
 public String getStatus(){return status;}public void setStatus(String v){status=v;}
 public Instant getCreatedAt(){return createdAt;}
}
