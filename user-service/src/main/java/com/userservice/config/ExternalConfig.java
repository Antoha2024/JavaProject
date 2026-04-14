package com.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * External Configuration паттерн.
 * 
 * Конфигурация вынесена из кода и может быть переопределена через:
 * - application.properties файлы
 * - Переменные окружения (${VAR_NAME:default})
 * - JVM параметры (-Dproperty=value)
 * 
 * @author Antoha2024
 */
@Configuration
@PropertySources({
    @PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = false),
    @PropertySource(value = "file:${CONFIG_DIR:/etc/config}/application.properties", ignoreResourceNotFound = true),
    @PropertySource(value = "file:${user.home}/.service/config.properties", ignoreResourceNotFound = true)
})
public class ExternalConfig {
    
    // Database configuration
    @Value("${db.driver:org.postgresql.Driver}")
    private String dbDriver;
    
    @Value("${db.url:jdbc:postgresql://localhost:5432/usersdb}")
    private String dbUrl;
    
    @Value("${db.username:postgres}")
    private String dbUsername;
    
    @Value("${db.password:${DB_PASSWORD:password}}")
    private String dbPassword;
    
    // Server configuration
    @Value("${server.port:8080}")
    private int serverPort;
    
    @Value("${spring.application.name:user-service}")
    private String serviceName;
    
    @Value("${service.host:localhost}")
    private String serviceHost;
    
    // Discovery configuration
    @Value("${discovery.server.url:${DISCOVERY_URL:http://localhost:8765}}")
    private String discoveryUrl;
    
    // Kafka configuration
    @Value("${kafka.bootstrap.servers:${KAFKA_BOOTSTRAP:localhost:9092}}")
    private String kafkaBootstrapServers;
    
    // Gateway configuration
    @Value("${gateway.url:${GATEWAY_URL:http://localhost:8080}}")
    private String gatewayUrl;
    
    // Circuit Breaker configuration
    @Value("${circuit.breaker.notification.threshold:3}")
    private int notificationCircuitThreshold;
    
    @Value("${circuit.breaker.notification.timeout:30000}")
    private long notificationCircuitTimeout;
    
    @Value("${circuit.breaker.notification.halfOpen:2}")
    private int notificationCircuitHalfOpen;
    
    // Getters
    public String getDbDriver() { return dbDriver; }
    public String getDbUrl() { return dbUrl; }
    public String getDbUsername() { return dbUsername; }
    public String getDbPassword() { return dbPassword; }
    public int getServerPort() { return serverPort; }
    public String getServiceName() { return serviceName; }
    public String getServiceHost() { return serviceHost; }
    public String getDiscoveryUrl() { return discoveryUrl; }
    public String getKafkaBootstrapServers() { return kafkaBootstrapServers; }
    public String getGatewayUrl() { return gatewayUrl; }
    public int getNotificationCircuitThreshold() { return notificationCircuitThreshold; }
    public long getNotificationCircuitTimeout() { return notificationCircuitTimeout; }
    public int getNotificationCircuitHalfOpen() { return notificationCircuitHalfOpen; }
}