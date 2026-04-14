package com.userservice.discovery;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ручная реализация Service Registry.
 * Хранит информацию о живых сервисах.
 * 
 * Аналог Netflix Eureka без Spring Boot.
 */
public class ServiceRegistry {
    // serviceName -> (instanceId -> ServiceInstance)
    private static final Map<String, Map<String, ServiceInstance>> registry = new ConcurrentHashMap<>();
    
    public static void register(String serviceName, String instanceId, String host, int port) {
        registry.computeIfAbsent(serviceName, k -> new ConcurrentHashMap<>())
                .put(instanceId, new ServiceInstance(instanceId, host, port, System.currentTimeMillis()));
        System.out.println("[ServiceRegistry] Registered: " + serviceName + "/" + instanceId + " at " + host + ":" + port);
    }
    
    public static void heartbeat(String serviceName, String instanceId) {
        Map<String, ServiceInstance> instances = registry.get(serviceName);
        if (instances != null && instances.containsKey(instanceId)) {
            instances.get(instanceId).setLastHeartbeat(System.currentTimeMillis());
            System.out.println("[ServiceRegistry] Heartbeat: " + serviceName + "/" + instanceId);
        }
    }
    
    public static void deregister(String serviceName, String instanceId) {
        Map<String, ServiceInstance> instances = registry.get(serviceName);
        if (instances != null) {
            instances.remove(instanceId);
            System.out.println("[ServiceRegistry] Deregistered: " + serviceName + "/" + instanceId);
        }
    }
    
    public static String getServiceUrl(String serviceName) {
        Map<String, ServiceInstance> instances = registry.get(serviceName);
        if (instances == null || instances.isEmpty()) {
            return null;
        }
        // Simple round-robin would go here, returning first for now
        ServiceInstance instance = instances.values().iterator().next();
        return "http://" + instance.getHost() + ":" + instance.getPort();
    }
    
    // Для очистки мёртвых инстансов (запускать по расписанию)
    public static void evictDeadInstances(long timeoutMs) {
        long now = System.currentTimeMillis();
        registry.values().forEach(instances -> 
            instances.entrySet().removeIf(entry -> 
                now - entry.getValue().getLastHeartbeat() > timeoutMs
            )
        );
    }
    
    public static class ServiceInstance {
        private final String instanceId;
        private final String host;
        private final int port;
        private long lastHeartbeat;
        
        public ServiceInstance(String instanceId, String host, int port, long lastHeartbeat) {
            this.instanceId = instanceId;
            this.host = host;
            this.port = port;
            this.lastHeartbeat = lastHeartbeat;
        }
        
        public String getHost() { return host; }
        public int getPort() { return port; }
        public long getLastHeartbeat() { return lastHeartbeat; }
        public void setLastHeartbeat(long lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    }
}