package com.ashish.banking.accountservice.domain;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.Instant;
@Entity @Table(name="bank_accounts",uniqueConstraints=@UniqueConstraint(columnNames="accountNumber"))
public class BankAccount {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=40) private String accountNumber;
 @Column(nullable=false) private Long customerId;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal balance=BigDecimal.ZERO;
 @Column(nullable=false,length=3) private String currency="INR";
 @Column(nullable=false,length=20) private String status="ACTIVE";
 @Column(nullable=false) private Instant createdAt=Instant.now();
 @Version private Long version;
 public Long getId(){return id;} public String getAccountNumber(){return accountNumber;} public void setAccountNumber(String v){accountNumber=v;}
 public Long getCustomerId(){return customerId;} public void setCustomerId(Long v){customerId=v;}
 public BigDecimal getBalance(){return balance;} public void setBalance(BigDecimal v){balance=v;}
 public String getCurrency(){return currency;} public void setCurrency(String v){currency=v;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;}
 public Instant getCreatedAt(){return createdAt;}
}
