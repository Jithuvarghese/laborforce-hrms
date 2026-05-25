package com.example.demo.workforce.event;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestTemplate;

@Component
public class SmsNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationListener.class);

    private final RestTemplate smsRestTemplate;

    @Value("${app.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${app.sms.url:https://example.invalid/sms}")
    private String smsUrl;

    public SmsNotificationListener(RestTemplate smsRestTemplate) {
        this.smsRestTemplate = smsRestTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSmsNotification(SmsNotificationEvent event) {
        if (!smsEnabled) {
            log.info("SMS notifications disabled. Skipping SMS to {}", event.phone());
            return;
        }

        Map<String, String> payload = Map.of(
                "phone", event.phone(),
                "message", event.message()
        );

        try {
            ResponseEntity<Void> response = smsRestTemplate.postForEntity(smsUrl, payload, Void.class);
            log.info("SMS request completed with status {} for {}", response.getStatusCode(), event.phone());
        } catch (Exception ex) {
            log.warn("Failed to send SMS to {}", event.phone(), ex);
        }
    }
}
