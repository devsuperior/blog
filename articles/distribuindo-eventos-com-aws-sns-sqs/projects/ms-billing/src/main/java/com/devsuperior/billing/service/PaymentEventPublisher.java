package com.devsuperior.billing.service;

import com.devsuperior.billing.event.PaymentProcessedEvent;
import io.awspring.cloud.sns.core.SnsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

    private final SnsTemplate snsTemplate;

    @Value("${app.topic.payment-events}")
    private String paymentEventsTopic;

    public PaymentEventPublisher(SnsTemplate snsTemplate) {
        this.snsTemplate = snsTemplate;
    }

    public void publish(PaymentProcessedEvent event) {
        log.info("Publicando PaymentProcessedEvent {} no topico {}",
                event.paymentId(), paymentEventsTopic);
        snsTemplate.sendNotification(paymentEventsTopic, event, null);
        log.info("PaymentProcessedEvent {} publicado com sucesso", event.paymentId());
    }
}
