package com.ashish.banking.customerservice.domain;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="customers")
public class Customer {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=120) private String fullName;
 @Column(nullable=false,unique=true,length=180) private String email;
 @Column(nullable=false,unique=true,length=30) private String phone;
 @Column(length=320) private String address;
 @Column(nullable=false,length=40) private String status="ACTIVE";
 @Column(nullable=false) private Instant createdAt=Instant.now();
 public Long getId(){return id;} public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
 public String getEmail(){return email;} public void setEmail(String v){email=v;}
 public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
 public String getAddress(){return address;} public void setAddress(String v){address=v;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;}
 public Instant getCreatedAt(){return createdAt;}
}
