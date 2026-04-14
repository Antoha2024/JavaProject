package com.userservice.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Клиент для взаимодействия с API Gateway.
 * 
 * API Gateway - единая точка входа для всех сервисов.
 * Обеспечивает маршрутизацию, агрегацию запросов.
 * 
 */
@Component
public class ApiGatewayClient {
    
    private final RestTemplate restTemplate;
    private final String gatewayUrl;
    
    public ApiGatewayClient(@Value("${gateway.url:http://localhost:8080}") String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Отправляет запрос через API Gateway
     */
    public <T> T sendRequest(String path, HttpMethod method, Object body, Class<T> responseType) {
        String url = gatewayUrl + path;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);
        return response.getBody();
    }
    
    /**
     * Получает статус всех сервисов через Gateway
     */
    public String getServicesStatus() {
        return sendRequest("/actuator/health", HttpMethod.GET, null, String.class);
    }
}