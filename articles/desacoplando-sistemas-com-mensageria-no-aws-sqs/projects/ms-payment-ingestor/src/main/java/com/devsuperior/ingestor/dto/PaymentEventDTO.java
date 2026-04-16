package com.devsuperior.ingestor.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentEventDTO(
        String paymentId,
        BigDecimal amount,
        String currency,
        String status,
        Instant createdAt
) {
}
