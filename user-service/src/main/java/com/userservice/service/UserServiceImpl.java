package com.userservice.service;

import com.userservice.circuitbreaker.CircuitBreaker;
import com.userservice.discovery.ServiceDiscoveryClient;
import com.userservice.dto.UserRequestDTO;
import com.userservice.dto.UserResponseDTO;
import com.userservice.entity.User;
import com.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional

/**
 * Реализация сервиса для управления пользователями.
 * 
 * Реализованы паттерны:
 * - Circuit Breaker: защита вызовов notification-service через CircuitBreaker
 * - Service Discovery: динамический поиск URL notification-service
 * - External Configuration: вынос конфигурации через ExternalConfig
 * - API Gateway: возможность отправки запросов через ApiGatewayClient
 * 
 * При недоступности notification-service используется fallback через Kafka.
 */

public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ServiceDiscoveryClient discoveryClient;
    private final CircuitBreaker notificationCircuitBreaker;
    private final RestTemplate restTemplate;
    
    private static final String USER_EVENTS_TOPIC = "user-events";
    
    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           KafkaTemplate<String, String> kafkaTemplate,
                           ServiceDiscoveryClient discoveryClient,
                           @Qualifier("notificationServiceCircuitBreaker") CircuitBreaker notificationCircuitBreaker) {
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.discoveryClient = discoveryClient;
        this.notificationCircuitBreaker = notificationCircuitBreaker;
        this.restTemplate = new RestTemplate();
    }
    
    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<UserResponseDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    @Override
    public Optional<UserResponseDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDTO);
    }
    
    @Override
    public UserResponseDTO createUser(UserRequestDTO userRequest) {
        User user = new User();
        user.setEmail(userRequest.getEmail());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setAge(userRequest.getAge());
        
        User savedUser = userRepository.save(user);
        
        sendNotificationWithCircuitBreaker(savedUser.getId(), "USER_CREATED", savedUser.getEmail());
        
        return convertToDTO(savedUser);
    }
    
    @Override
    public Optional<UserResponseDTO> updateUser(Long id, UserRequestDTO userRequest) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setEmail(userRequest.getEmail());
                    existingUser.setFirstName(userRequest.getFirstName());
                    existingUser.setLastName(userRequest.getLastName());
                    existingUser.setAge(userRequest.getAge());
                    
                    User updatedUser = userRepository.save(existingUser);
                    
                    sendNotificationWithCircuitBreaker(updatedUser.getId(), "USER_UPDATED", updatedUser.getEmail());
                    
                    return convertToDTO(updatedUser);
                });
    }
    
    @Override
    public boolean deleteUser(Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    sendNotificationWithCircuitBreaker(id, "USER_DELETED", user.getEmail());
                    return true;
                })
                .orElse(false);
    }
    
    @Override
    public int removeDuplicateUsers() {
        List<User> duplicates = userRepository.findDuplicateUsersByEmail();
        if (duplicates.isEmpty()) {
            return 0;
        }
        
        java.util.Map<String, List<User>> duplicatesByEmail = duplicates.stream()
                .collect(Collectors.groupingBy(User::getEmail));
        
        int removedCount = 0;
        for (java.util.Map.Entry<String, List<User>> entry : duplicatesByEmail.entrySet()) {
            String email = entry.getKey();
            List<User> users = entry.getValue();
            
            if (users.size() > 1) {
                users.sort((u1, u2) -> u1.getId().compareTo(u2.getId()));
                List<Long> idsToDelete = users.subList(1, users.size()).stream()
                        .map(User::getId)
                        .collect(Collectors.toList());
                
                removedCount += userRepository.deleteDuplicatesByEmail(email, idsToDelete);
            }
        }
        
        if (removedCount > 0) {
            sendNotificationWithCircuitBreaker(0L, "DUPLICATES_REMOVED", "Removed " + removedCount + " duplicates");
        }
        
        return removedCount;
    }
    
    private void sendNotificationWithCircuitBreaker(Long userId, String eventType, String data) {
        String notificationUrl = discoveryClient.getServiceUrl("notification-service");
        
        if (notificationUrl == null) {
            System.err.println("[UserService] Notification service not found, using Kafka fallback");
            sendUserEvent(userId, eventType, data);
            return;
        }
        
        String url = notificationUrl + "/api/notifications?userId=" + userId + "&eventType=" + eventType + "&data=" + data;
        
        notificationCircuitBreaker.execute(
            () -> {
                restTemplate.postForEntity(url, null, String.class);
                System.out.println("[UserService] Notification sent via HTTP to userId=" + userId);
                return null;
            },
            () -> {
                System.err.println("[UserService] Circuit Breaker OPEN - using Kafka fallback");
                sendUserEvent(userId, eventType, data);
                return null;
            }
        );
    }
    
    private void sendUserEvent(Long userId, String eventType, String data) {
        String message = String.format("{\"userId\":%d,\"eventType\":\"%s\",\"data\":\"%s\",\"timestamp\":%d}",
                userId, eventType, data, System.currentTimeMillis());
        kafkaTemplate.send(USER_EVENTS_TOPIC, message);
        System.out.println("[UserService] Kafka event sent: " + eventType);
    }
    
    private UserResponseDTO convertToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setAge(user.getAge());
        return dto;
    }
}