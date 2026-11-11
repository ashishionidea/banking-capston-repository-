package com.ashish.banking.billpaymentservice.api;
import com.ashish.banking.billpaymentservice.domain.BillPayment;import com.ashish.banking.billpaymentservice.repository.BillPaymentRepository;
import jakarta.validation.Valid;import jakarta.validation.constraints.*;import org.springframework.http.HttpStatus;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;import org.springframework.web.bind.annotation.*;import java.math.BigDecimal;import java.util.UUID;
@RestController @RequestMapping("/api/bill-payments")
public class BillPaymentController{
 private final BillPaymentRepository payments;public BillPaymentController(BillPaymentRepository p){payments=p;}
 public record Request(@NotBlank String billerCode,@NotBlank String customerReference,@NotBlank String accountNumber,@NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal amount,String currency){}
 public record Response(String paymentReference,String status,String message){}
 @PostMapping @ResponseStatus(HttpStatus.ACCEPTED) public Response pay(@Valid@RequestBody Request r,@AuthenticationPrincipal Jwt jwt){
  BillPayment p=new BillPayment();p.setReference("BP-"+UUID.randomUUID());p.setCustomerId(Long.valueOf(jwt.getSubject()));p.setBillerCode(r.billerCode());p.setCustomerReference(r.customerReference());p.setAccountNumber(r.accountNumber());p.setAmount(r.amount());p.setCurrency(r.currency()==null?"INR":r.currency().toUpperCase());payments.save(p);return new Response(p.getReference(),p.getStatus(),"Payment request accepted; biller settlement adapter is not configured");
 }
 @GetMapping("/{reference}") public Response status(@PathVariable String reference,@AuthenticationPrincipal Jwt jwt){BillPayment p=payments.findByReferenceAndCustomerId(reference,Long.valueOf(jwt.getSubject())).orElseThrow(()->new IllegalArgumentException("Payment not found"));return new Response(p.getReference(),p.getStatus(),p.getStatus());}
}
