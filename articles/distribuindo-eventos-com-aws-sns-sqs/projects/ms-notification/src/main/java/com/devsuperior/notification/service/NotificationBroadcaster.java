package com.devsuperior.notification.service;

import com.devsuperior.notification.event.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(NotificationBroadcaster.class);

    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emittersByPaymentId = new ConcurrentHashMap<>();

    public void register(String paymentId, SseEmitter emitter) {
        emittersByPaymentId
                .computeIfAbsent(paymentId, key -> new CopyOnWriteArrayList<>())
                .add(emitter);

        emitter.onCompletion(() -> removeEmitter(paymentId, emitter, "completion"));
        emitter.onTimeout(() -> removeEmitter(paymentId, emitter, "timeout"));
    }

    public void broadcast(PaymentProcessedEvent event) {
        List<SseEmitter> targets = emittersByPaymentId.get(event.paymentId());
        if (targets == null || targets.isEmpty()) {
            log.info("Nenhum cliente conectado ao stream do pagamento {}, evento descartado para SSE",
                    event.paymentId());
            return;
        }

        log.info("Broadcast do pagamento {} para {} cliente(s) conectado(s)",
                event.paymentId(), targets.size());

        for (SseEmitter emitter : targets) {
            try {
                emitter.send(SseEmitter.event()
                        .name("payment-processed")
                        .data(event));
            } catch (IOException e) {
                log.warn("Falha ao enviar evento para emitter do pagamento {}, removendo: {}",
                        event.paymentId(), e.getMessage());
                removeEmitter(event.paymentId(), emitter, "io-error");
            }
        }
    }

    private void removeEmitter(String paymentId, SseEmitter emitter, String reason) {
        CopyOnWriteArrayList<SseEmitter> list = emittersByPaymentId.get(paymentId);
        if (list == null) {
            return;
        }
        list.remove(emitter);
        if (list.isEmpty()) {
            emittersByPaymentId.remove(paymentId, list);
        }
        log.info("Emitter do pagamento {} removido (motivo: {}), restam {}",
                paymentId, reason, list.size());
    }
}
