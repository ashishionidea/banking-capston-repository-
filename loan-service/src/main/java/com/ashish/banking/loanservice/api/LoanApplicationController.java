package com.ashish.banking.loanservice.api;
import com.ashish.banking.loanservice.domain.LoanApplication;import com.ashish.banking.loanservice.repository.LoanApplicationRepository;
import jakarta.validation.Valid;import jakarta.validation.constraints.*;import org.springframework.http.HttpStatus;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;import org.springframework.web.bind.annotation.*;import java.math.BigDecimal;import java.util.UUID;
@RestController @RequestMapping("/api/loans/applications")
public class LoanApplicationController{
 private final LoanApplicationRepository loans;public LoanApplicationController(LoanApplicationRepository l){loans=l;}
 public record Request(@NotNull @DecimalMin("1.00") @Digits(integer=17,fraction=2) BigDecimal requestedAmount,@NotBlank @Size(max=500) String purpose){}
 public record Response(String applicationReference,String status,String message){}
 @PostMapping @ResponseStatus(HttpStatus.ACCEPTED)public Response apply(@Valid@RequestBody Request r,@AuthenticationPrincipal Jwt jwt){LoanApplication a=new LoanApplication();a.setReference("LN-"+UUID.randomUUID());a.setCustomerId(Long.valueOf(jwt.getSubject()));a.setRequestedAmount(r.requestedAmount());a.setPurpose(r.purpose());loans.save(a);return new Response(a.getReference(),a.getStatus(),"Application acknowledged; eligibility and underwriting policy must be configured");}
 @GetMapping("/{reference}")public Response status(@PathVariable String reference,@AuthenticationPrincipal Jwt jwt){LoanApplication a=loans.findByReferenceAndCustomerId(reference,Long.valueOf(jwt.getSubject())).orElseThrow(()->new IllegalArgumentException("Application not found"));return new Response(a.getReference(),a.getStatus(),a.getStatus());}
}
