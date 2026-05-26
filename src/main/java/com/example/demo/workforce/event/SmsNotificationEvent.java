package com.example.demo.workforce.event;

public record SmsNotificationEvent(
        String phone,
        String message
) {
}
