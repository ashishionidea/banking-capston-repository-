package com.ashish.banking.transferservice.messaging;
import java.math.BigDecimal;
public record TransferEvent(String reference,Long customerId,String sourceAccountNumber,String destinationAccountNumber,BigDecimal amount,String currency,String reason){}
