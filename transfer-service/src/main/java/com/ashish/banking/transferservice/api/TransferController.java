package com.ashish.banking.transferservice.api;
import com.ashish.banking.transferservice.domain.TransferSaga;import com.ashish.banking.transferservice.service.TransferService;import jakarta.validation.Valid;import jakarta.validation.constraints.*;import org.springframework.http.HttpStatus;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;import org.springframework.web.bind.annotation.*;import java.math.BigDecimal;
@RestController @RequestMapping("/api/transfers")
public class TransferController{
 private final TransferService service;public TransferController(TransferService s){service=s;}
 public record Request(@NotBlank String sourceAccountNumber,@NotBlank String destinationAccountNumber,@NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal amount,String currency){}
 public record Response(String reference,String status,String failureReason){}
 @PostMapping @ResponseStatus(HttpStatus.ACCEPTED) public Response transfer(@RequestHeader("Idempotency-Key")@NotBlank String key,@Valid@RequestBody Request r,@AuthenticationPrincipal Jwt jwt){TransferSaga s=service.start(key,Long.valueOf(jwt.getSubject()),r.sourceAccountNumber(),r.destinationAccountNumber(),r.amount(),r.currency()==null?"INR":r.currency().toUpperCase());return view(s);}
 @GetMapping("/{reference}") public Response status(@PathVariable String reference,@AuthenticationPrincipal Jwt jwt){return view(service.get(reference,Long.valueOf(jwt.getSubject())));}
 private Response view(TransferSaga s){return new Response(s.getReference(),s.getStatus(),s.getFailureReason());}
}
