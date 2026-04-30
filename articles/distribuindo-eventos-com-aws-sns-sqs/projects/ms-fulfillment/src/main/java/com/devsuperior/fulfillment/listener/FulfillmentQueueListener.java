package com.devsuperior.fulfillment.listener;

import com.devsuperior.fulfillment.event.PaymentProcessedEvent;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FulfillmentQueueListener {

    private static final Logger log = LoggerFactory.getLogger(FulfillmentQueueListener.class);

    private final FulfillmentService fulfillmentService;

    public FulfillmentQueueListener(FulfillmentService fulfillmentService) {
        this.fulfillmentService = fulfillmentService;
    }

    @SqsListener("${app.queue.fulfillment}")
    public void onPaymentProcessed(PaymentProcessedEvent event) {
        log.info("Fulfillment recebido para pagamento {}", event.paymentId());
        fulfillmentService.releaseProduct(event);
    }
}
