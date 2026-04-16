package com.devsuperior.billing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "processed_payments")
public class ProcessedPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String paymentId;

    @Column(nullable = false)
    private BigDecimal originalAmount;

    @Column(nullable = false, length = 3)
    private String originalCurrency;

    @Column(nullable = false)
    private BigDecimal convertedAmountUsd;

    @Column(nullable = false)
    private BigDecimal taxAmount;

    @Column(nullable = false)
    private BigDecimal netAmount;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant processedAt;

    public ProcessedPayment() {
    }

    public ProcessedPayment(String paymentId, BigDecimal originalAmount, String originalCurrency,
                            BigDecimal convertedAmountUsd, BigDecimal taxAmount,
                            BigDecimal netAmount, String status, Instant processedAt) {
        this.paymentId = paymentId;
        this.originalAmount = originalAmount;
        this.originalCurrency = originalCurrency;
        this.convertedAmountUsd = convertedAmountUsd;
        this.taxAmount = taxAmount;
        this.netAmount = netAmount;
        this.status = status;
        this.processedAt = processedAt;
    }

    public Long getId() { return id; }
    public String getPaymentId() { return paymentId; }
    public BigDecimal getOriginalAmount() { return originalAmount; }
    public String getOriginalCurrency() { return originalCurrency; }
    public BigDecimal getConvertedAmountUsd() { return convertedAmountUsd; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getNetAmount() { return netAmount; }
    public String getStatus() { return status; }
    public Instant getProcessedAt() { return processedAt; }
}
