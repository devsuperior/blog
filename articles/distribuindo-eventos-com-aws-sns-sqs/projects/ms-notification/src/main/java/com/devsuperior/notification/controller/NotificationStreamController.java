package com.devsuperior.notification.controller;

import com.devsuperior.notification.service.NotificationBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
public class NotificationStreamController {

    private static final Logger log = LoggerFactory.getLogger(NotificationStreamController.class);

    private final NotificationBroadcaster broadcaster;

    public NotificationStreamController(NotificationBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @GetMapping(path = "/stream/{paymentId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String paymentId) {
        SseEmitter emitter = new SseEmitter(0L); // 0 = sem timeout (mantem aberto)
        broadcaster.register(paymentId, emitter);
        log.info("Novo cliente conectado ao stream do pagamento {}", paymentId);
        return emitter;
    }
}
