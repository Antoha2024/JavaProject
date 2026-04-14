package com.notificationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationservice.dto.UserEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private static final String TOPIC = "user-events";

    private final EmailService emailService;  // ← используем интерфейс
    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaConsumerService(EmailService emailService) {
        this.emailService = emailService;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = TOPIC, groupId = "notification-service-group")
    public void listen(String message) {
        try {
            UserEventDTO event = objectMapper.readValue(message, UserEventDTO.class);
            logger.info("Received event: operation={}, email={}", event.getOperation(), event.getEmail());

            emailService.sendEmail(event, event.getEmail());
            logger.info("Email sent to: {}", event.getEmail());

        } catch (Exception e) {
            logger.error("Failed to process Kafka message: {}", message, e);
        }
    }
}