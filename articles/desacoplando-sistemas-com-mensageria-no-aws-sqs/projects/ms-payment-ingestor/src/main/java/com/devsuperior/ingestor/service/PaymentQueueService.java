package com.devsuperior.ingestor.service;

import com.devsuperior.ingestor.dto.PaymentEventDTO;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentQueueService {

    private static final Logger log = LoggerFactory.getLogger(PaymentQueueService.class);

    private final SqsTemplate sqsTemplate;

    @Value("${app.queue.billing}")
    private String billingQueue;

    public PaymentQueueService(SqsTemplate sqsTemplate) {
        this.sqsTemplate = sqsTemplate;
    }

    public void enqueue(PaymentEventDTO event) {
        log.info("Enfileirando pagamento {} na billing-queue", event.paymentId());
        sqsTemplate.send(billingQueue, event);
        log.info("Pagamento {} enfileirado com sucesso", event.paymentId());
    }
}
