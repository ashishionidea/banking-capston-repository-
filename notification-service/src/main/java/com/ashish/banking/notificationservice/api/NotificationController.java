package com.ashish.banking.notificationservice.api;
import com.ashish.banking.notificationservice.domain.NotificationRecord;import com.ashish.banking.notificationservice.repository.NotificationRecordRepository;import jakarta.validation.Valid;import jakarta.validation.constraints.*;import org.springframework.http.HttpStatus;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/internal/notifications") public class NotificationController{
 private final NotificationRecordRepository records;public NotificationController(NotificationRecordRepository r){records=r;}
 public record Request(@NotBlank String eventId,@NotBlank String channel,@NotBlank String recipient,@NotBlank @Size(max=2000)String message){}
 public record Response(String eventId,String status,String maskedRecipient){}
 @PostMapping @ResponseStatus(HttpStatus.ACCEPTED) public Response submit(@Valid@RequestBody Request r){NotificationRecord n=records.findByEventId(r.eventId()).orElseGet(()->{NotificationRecord x=new NotificationRecord();x.setEventId(r.eventId());x.setChannel(r.channel());x.setRecipientMasked(mask(r.recipient()));x.setMessage(r.message());return records.save(x);});return new Response(n.getEventId(),n.getStatus(),n.getRecipientMasked());}
 private String mask(String v){if(v.length()<4)return "****";return "****"+v.substring(v.length()-4);}
}
