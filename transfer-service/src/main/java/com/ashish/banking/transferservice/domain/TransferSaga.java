package com.ashish.banking.transferservice.domain;
import jakarta.persistence.*;import java.math.BigDecimal;import java.time.Instant;
@Entity @Table(name="transfer_sagas",uniqueConstraints=@UniqueConstraint(columnNames="idempotencyKey"))
public class TransferSaga{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true) private String reference;
 @Column(nullable=false,unique=true) private String idempotencyKey;
 @Column(nullable=false) private Long customerId;
 @Column(nullable=false) private String sourceAccountNumber;
 @Column(nullable=false) private String destinationAccountNumber;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal amount;
 @Column(nullable=false,length=3) private String currency;
 @Column(nullable=false) private String status;
 @Column(length=1000) private String failureReason;
 @Column(nullable=false) private Instant createdAt=Instant.now();
 @Column(nullable=false) private Instant updatedAt=Instant.now();
 @PreUpdate void touch(){updatedAt=Instant.now();}
 public Long getId(){return id;} public String getReference(){return reference;} public void setReference(String v){reference=v;}
 public String getIdempotencyKey(){return idempotencyKey;} public void setIdempotencyKey(String v){idempotencyKey=v;}
 public Long getCustomerId(){return customerId;} public void setCustomerId(Long v){customerId=v;}
 public String getSourceAccountNumber(){return sourceAccountNumber;} public void setSourceAccountNumber(String v){sourceAccountNumber=v;}
 public String getDestinationAccountNumber(){return destinationAccountNumber;} public void setDestinationAccountNumber(String v){destinationAccountNumber=v;}
 public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;}
 public String getCurrency(){return currency;} public void setCurrency(String v){currency=v;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;}
 public String getFailureReason(){return failureReason;} public void setFailureReason(String v){failureReason=v;}
}
