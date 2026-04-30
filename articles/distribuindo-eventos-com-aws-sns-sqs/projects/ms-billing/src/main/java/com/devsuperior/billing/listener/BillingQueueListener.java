package com.devsuperior.billing.listener;

import com.devsuperior.billing.dto.PaymentReceivedEvent;
import com.devsuperior.billing.service.BillingProcessorService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BillingQueueListener {

    private static final Logger log = LoggerFactory.getLogger(BillingQueueListener.class);

    private final BillingProcessorService processorService;

    public BillingQueueListener(BillingProcessorService processorService) {
        this.processorService = processorService;
    }

    @SqsListener("${app.queue.billing}")
    public void onPaymentReceived(PaymentReceivedEvent event) {
        log.info("Pagamento recebido da fila: {}", event.paymentId());
        processorService.process(event);
        log.info("Pagamento {} processado com sucesso", event.paymentId());
    }
}
