package com.devsuperior.billing.service;

import com.devsuperior.billing.client.CurrencyConverter;
import com.devsuperior.billing.dto.PaymentReceivedEvent;
import com.devsuperior.billing.entity.ProcessedPayment;
import com.devsuperior.billing.event.PaymentProcessedEvent;
import com.devsuperior.billing.repository.ProcessedPaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
public class BillingProcessorService {

    private static final Logger log = LoggerFactory.getLogger(BillingProcessorService.class);

    private static final BigDecimal TAX_RATE = new BigDecimal("0.0365");

    private final ProcessedPaymentRepository repository;
    private final CurrencyConverter currencyConverter;
    private final PaymentEventPublisher eventPublisher;

    public BillingProcessorService(ProcessedPaymentRepository repository,
                                   CurrencyConverter currencyConverter,
                                   PaymentEventPublisher eventPublisher) {
        this.repository = repository;
        this.currencyConverter = currencyConverter;
        this.eventPublisher = eventPublisher;
    }

    public void process(PaymentReceivedEvent event) {
        // Idempotencia, heranca do episodio 1
        if (repository.existsByPaymentId(event.paymentId())) {
            log.warn("Pagamento {} ja foi processado, ignorando duplicata", event.paymentId());
            return;
        }

        log.info("Processando pagamento {} (valor: {} {})",
                event.paymentId(), event.amount(), event.currency());

        BigDecimal amountUsd = currencyConverter.toUsd(event.amount(), event.currency());
        BigDecimal taxAmount = amountUsd.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netAmount = amountUsd.subtract(taxAmount).setScale(2, RoundingMode.HALF_UP);

        log.info("Calculo fiscal: bruto USD={}, imposto={}, liquido={}",
                amountUsd.setScale(2, RoundingMode.HALF_UP), taxAmount, netAmount);

        ProcessedPayment payment = new ProcessedPayment(
                event.paymentId(),
                event.amount(),
                event.currency(),
                amountUsd.setScale(2, RoundingMode.HALF_UP),
                taxAmount,
                netAmount,
                "PROCESSED",
                Instant.now()
        );
        repository.save(payment);

        log.info("Fatura persistida para pagamento {}, liquido: {} USD",
                event.paymentId(), netAmount);

        // Fan-out: publica o evento de dominio para todos os interessados
        PaymentProcessedEvent domainEvent = new PaymentProcessedEvent(
                payment.getPaymentId(),
                payment.getConvertedAmountUsd(),
                payment.getTaxAmount(),
                payment.getProcessedAt()
        );
        eventPublisher.publish(domainEvent);
    }
}
