package com.ashish.banking.billpaymentservice.domain;
import jakarta.persistence.*;import java.math.BigDecimal;import java.time.Instant;
@Entity @Table(name="bill_payments")
public class BillPayment{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true) private String reference;
 @Column(nullable=false) private Long customerId;
 @Column(nullable=false) private String billerCode;
 @Column(nullable=false) private String customerReference;
 @Column(nullable=false) private String accountNumber;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal amount;
 @Column(nullable=false,length=3) private String currency="INR";
 @Column(nullable=false) private String status="SUBMITTED";
 @Column(nullable=false) private Instant createdAt=Instant.now();
 public Long getId(){return id;}public String getReference(){return reference;}public void setReference(String v){reference=v;}
 public Long getCustomerId(){return customerId;}public void setCustomerId(Long v){customerId=v;}
 public String getBillerCode(){return billerCode;}public void setBillerCode(String v){billerCode=v;}
 public String getCustomerReference(){return customerReference;}public void setCustomerReference(String v){customerReference=v;}
 public String getAccountNumber(){return accountNumber;}public void setAccountNumber(String v){accountNumber=v;}
 public BigDecimal getAmount(){return amount;}public void setAmount(BigDecimal v){amount=v;}
 public String getCurrency(){return currency;}public void setCurrency(String v){currency=v;}
 public String getStatus(){return status;}public void setStatus(String v){status=v;}public Instant getCreatedAt(){return createdAt;}
}
