package com.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.userservice.dto.UserEventDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Сервис для отправки событий о пользователях в Kafka
 * При создании или удалении пользователя отправляет сообщение в топик user-events
 */
@Service
public class KafkaEventProducerService {
    
    private static final String TOPIC = "user-events";
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public KafkaEventProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Отправляет событие о пользователе в Kafka
     * @param operation тип операции (CREATE/DELETE)
     * @param email email пользователя
     */
    public void sendUserEvent(UserEventDTO.OperationType operation, String email) {
        UserEventDTO event = new UserEventDTO(operation, email);
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize user event", e);
        }
    }
}