package com.devsuperior.fulfillment.event;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentProcessedEvent(
        String paymentId,
        BigDecimal processedAmountUsd,
        BigDecimal processedTaxUsd,
        Instant processedAt
) {
}
