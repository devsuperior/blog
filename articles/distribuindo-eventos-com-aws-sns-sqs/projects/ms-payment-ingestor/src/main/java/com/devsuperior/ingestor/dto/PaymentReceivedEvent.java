package com.devsuperior.ingestor.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentReceivedEvent(
        String paymentId,
        BigDecimal amount,
        String currency,
        String status,
        Instant createdAt
) {
}
