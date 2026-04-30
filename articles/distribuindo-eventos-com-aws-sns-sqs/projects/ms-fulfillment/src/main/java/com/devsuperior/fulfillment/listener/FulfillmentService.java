package com.devsuperior.fulfillment.listener;

import com.devsuperior.fulfillment.event.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FulfillmentService {

    private static final Logger log = LoggerFactory.getLogger(FulfillmentService.class);

    public void releaseProduct(PaymentProcessedEvent event) {
        log.info("Liberando produto/servico para pagamento {}, valor liquido {} USD",
                event.paymentId(),
                event.processedAmountUsd().subtract(event.processedTaxUsd()));
    }
}
