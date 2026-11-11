package com.ashish.banking.loanservice.domain;
import jakarta.persistence.*;import java.math.BigDecimal;import java.time.Instant;
@Entity @Table(name="loan_applications")
public class LoanApplication{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;
 @Column(nullable=false,unique=true)private String reference;
 @Column(nullable=false)private Long customerId;
 @Column(nullable=false,precision=19,scale=2)private BigDecimal requestedAmount;
 @Column(nullable=false,length=500)private String purpose;
 @Column(nullable=false,length=30)private String status="RECEIVED";
 @Column(nullable=false)private Instant createdAt=Instant.now();
 public Long getId(){return id;}public String getReference(){return reference;}public void setReference(String v){reference=v;}
 public Long getCustomerId(){return customerId;}public void setCustomerId(Long v){customerId=v;}
 public BigDecimal getRequestedAmount(){return requestedAmount;}public void setRequestedAmount(BigDecimal v){requestedAmount=v;}
 public String getPurpose(){return purpose;}public void setPurpose(String v){purpose=v;}
 public String getStatus(){return status;}public void setStatus(String v){status=v;}public Instant getCreatedAt(){return createdAt;}
}
