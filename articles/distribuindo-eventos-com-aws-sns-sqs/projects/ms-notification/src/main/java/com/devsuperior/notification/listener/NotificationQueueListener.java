package com.devsuperior.notification.listener;

import com.devsuperior.notification.event.PaymentProcessedEvent;
import com.devsuperior.notification.service.NotificationBroadcaster;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationQueueListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationQueueListener.class);

    private final NotificationBroadcaster broadcaster;

    public NotificationQueueListener(NotificationBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @SqsListener("${app.queue.notification}")
    public void onPaymentProcessed(PaymentProcessedEvent event) {
        log.info("Notificacao recebida da fila para pagamento {}", event.paymentId());
        broadcaster.broadcast(event);
    }
}
