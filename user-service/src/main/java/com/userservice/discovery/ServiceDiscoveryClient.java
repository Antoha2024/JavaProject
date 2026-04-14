package com.userservice.discovery;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.UUID;

/**
 * Клиент для регистрации сервиса в Service Registry.
 * Отправляет HTTP-запросы к discovery-server.
 * 
 * Заменяет @EnableDiscoveryClient.
 * 
 */
public class ServiceDiscoveryClient {
    private final String discoveryServerUrl;
    private final String serviceName;
    private final String instanceId;
    private final String host;
    private final int port;
    private final RestTemplate restTemplate;
    private boolean registered = false;
    
    public ServiceDiscoveryClient(String discoveryServerUrl, String serviceName, String host, int port) {
        this.discoveryServerUrl = discoveryServerUrl;
        this.serviceName = serviceName;
        this.instanceId = serviceName + "-" + UUID.randomUUID().toString().substring(0, 8);
        this.host = host;
        this.port = port;
        this.restTemplate = new RestTemplate();
    }
    
    public void register() {
        try {
            String url = discoveryServerUrl + "/register?serviceName=" + serviceName + 
                        "&instanceId=" + instanceId + "&host=" + host + "&port=" + port;
            restTemplate.postForEntity(url, null, String.class);
            registered = true;
            System.out.println("[ServiceDiscoveryClient] Registered: " + serviceName);
        } catch (Exception e) {
            System.err.println("[ServiceDiscoveryClient] Registration failed: " + e.getMessage());
        }
    }
    
    public void sendHeartbeat() {
        if (!registered) return;
        try {
            String url = discoveryServerUrl + "/heartbeat?serviceName=" + serviceName + "&instanceId=" + instanceId;
            restTemplate.postForEntity(url, null, String.class);
        } catch (Exception e) {
            System.err.println("[ServiceDiscoveryClient] Heartbeat failed: " + e.getMessage());
        }
    }
    
    public void deregister() {
        if (!registered) return;
        try {
            String url = discoveryServerUrl + "/deregister?serviceName=" + serviceName + "&instanceId=" + instanceId;
            restTemplate.postForEntity(url, null, String.class);
        } catch (Exception e) {
            System.err.println("[ServiceDiscoveryClient] Deregistration failed: " + e.getMessage());
        }
    }
    
    public String getServiceUrl(String targetServiceName) {
        try {
            String url = discoveryServerUrl + "/getUrl?serviceName=" + targetServiceName;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("[ServiceDiscoveryClient] Failed to get URL for " + targetServiceName);
            return null;
        }
    }
}