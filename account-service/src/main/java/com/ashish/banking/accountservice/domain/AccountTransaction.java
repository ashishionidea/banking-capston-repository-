package com.ashish.banking.accountservice.domain;
import jakarta.persistence.*;import java.math.BigDecimal;import java.time.Instant;
@Entity @Table(name="account_transactions",uniqueConstraints=@UniqueConstraint(columnNames="reference"))
public class AccountTransaction{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;
 @Column(nullable=false,unique=true)private String reference;
 @Column(nullable=false)private String accountNumber;
 @Column(nullable=false)private String type;
 @Column(nullable=false)private String status;
 @Column(nullable=false,precision=19,scale=2)private BigDecimal amount;
 @Column(nullable=false,length=3)private String currency;
 @Column(nullable=false)private Instant createdAt=Instant.now();
 public Long getId(){return id;}public String getReference(){return reference;}public void setReference(String v){reference=v;}
 public String getAccountNumber(){return accountNumber;}public void setAccountNumber(String v){accountNumber=v;}
 public String getType(){return type;}public void setType(String v){type=v;}
 public String getStatus(){return status;}public void setStatus(String v){status=v;}
 public BigDecimal getAmount(){return amount;}public void setAmount(BigDecimal v){amount=v;}
 public String getCurrency(){return currency;}public void setCurrency(String v){currency=v;}
 public Instant getCreatedAt(){return createdAt;}public void setCreatedAt(Instant v){createdAt=v;}
}
