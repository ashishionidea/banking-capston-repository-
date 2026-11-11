package com.ashish.banking.supportservice.api;
import com.ashish.banking.supportservice.domain.SupportTicket;import com.ashish.banking.supportservice.repository.SupportTicketRepository;
import jakarta.validation.Valid;import jakarta.validation.constraints.*;import org.springframework.http.HttpStatus;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;import org.springframework.web.bind.annotation.*;import java.util.UUID;
@RestController @RequestMapping("/api/support/tickets")
public class SupportTicketController{
 private final SupportTicketRepository tickets;public SupportTicketController(SupportTicketRepository t){tickets=t;}
 public record Request(@NotBlank @Size(max=180)String subject,@NotBlank @Size(max=4000)String description){}
 public record Response(String ticketReference,String status,String message){}
 @PostMapping @ResponseStatus(HttpStatus.CREATED)public Response create(@Valid@RequestBody Request r,@AuthenticationPrincipal Jwt jwt){SupportTicket t=new SupportTicket();t.setReference("TK-"+UUID.randomUUID());t.setCustomerId(Long.valueOf(jwt.getSubject()));t.setSubject(r.subject());t.setDescription(r.description());tickets.save(t);return new Response(t.getReference(),t.getStatus(),"Support request acknowledged");}
 @GetMapping("/{reference}")public Response get(@PathVariable String reference,@AuthenticationPrincipal Jwt jwt){SupportTicket t=tickets.findByReferenceAndCustomerId(reference,Long.valueOf(jwt.getSubject())).orElseThrow(()->new IllegalArgumentException("Ticket not found"));return new Response(t.getReference(),t.getStatus(),t.getStatus());}
}
