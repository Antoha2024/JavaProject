package com.userservice.circuitbreaker;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Circuit Breaker паттерна.
 * 
 * Определяет настройки предохранителя для различных сервисов:
 * - notificationServiceCircuitBreaker: защита вызовов notification-service
 *   (3 ошибки -> OPEN на 30 секунд -> 2 успеха для восстановления)
 * 
 */

@Configuration
public class CircuitBreakerConfig {
    
    @Bean
    public CircuitBreaker notificationServiceCircuitBreaker() {
        // 3 ошибки -> OPEN, ждём 30 секунд, 2 успешных запроса для закрытия
        return new CircuitBreaker(3, 30000, 2);
    }
    
    @Bean
    public CircuitBreaker databaseCircuitBreaker() {
        // 5 ошибок -> OPEN, ждём 60 секунд, 3 успешных запроса для закрытия
        return new CircuitBreaker(5, 60000, 3);
    }
}