package com.discovery;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP сервер для Service Discovery.
 * Заменяет Netflix Eureka.
 * 
 * Эндпоинты:
 * POST /register?serviceName=X&instanceId=Y&host=Z&port=P
 * POST /heartbeat?serviceName=X&instanceId=Y
 * POST /deregister?serviceName=X&instanceId=Y
 * GET  /getUrl?serviceName=X
 */
public class DiscoveryServer {
    
    private static final Map<String, Map<String, ServiceInstance>> registry = new ConcurrentHashMap<>();
    private static final long EVICTION_INTERVAL_MS = 30000;
    private static final long HEARTBEAT_TIMEOUT_MS = 25000;
    
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getProperty("discovery.port", "8765"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        server.createContext("/register", new RegisterHandler());
        server.createContext("/heartbeat", new HeartbeatHandler());
        server.createContext("/deregister", new DeregisterHandler());
        server.createContext("/getUrl", new GetUrlHandler());
        
        // Добавляем health-check эндпоинт для Docker
        server.createContext("/actuator/health", new HealthCheckHandler());
        
        server.setExecutor(null);
        server.start();
        
        startEvictionThread();
        
        System.out.println("Discovery Server started on port " + port);
    }
    
    static class ServiceInstance {
        String instanceId, host;
        int port;
        long lastHeartbeat;
        
        ServiceInstance(String instanceId, String host, int port, long lastHeartbeat) {
            this.instanceId = instanceId;
            this.host = host;
            this.port = port;
            this.lastHeartbeat = lastHeartbeat;
        }
    }
    
    static class HealthCheckHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"status\":\"UP\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    
    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            
            Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
            String serviceName = params.get("serviceName");
            String instanceId = params.get("instanceId");
            String host = params.get("host");
            int port = Integer.parseInt(params.get("port"));
            
            registry.computeIfAbsent(serviceName, k -> new ConcurrentHashMap<>())
                    .put(instanceId, new ServiceInstance(instanceId, host, port, System.currentTimeMillis()));
            
            String response = "Registered: " + serviceName + "/" + instanceId;
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            System.out.println(response);
        }
    }
    
    static class HeartbeatHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            
            Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
            String serviceName = params.get("serviceName");
            String instanceId = params.get("instanceId");
            
            Map<String, ServiceInstance> instances = registry.get(serviceName);
            if (instances != null && instances.containsKey(instanceId)) {
                instances.get(instanceId).lastHeartbeat = System.currentTimeMillis();
                exchange.sendResponseHeaders(200, -1);
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        }
    }
    
    static class DeregisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            
            Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
            String serviceName = params.get("serviceName");
            String instanceId = params.get("instanceId");
            
            Map<String, ServiceInstance> instances = registry.get(serviceName);
            if (instances != null) {
                instances.remove(instanceId);
            }
            exchange.sendResponseHeaders(200, -1);
            System.out.println("Deregistered: " + serviceName + "/" + instanceId);
        }
    }
    
    static class GetUrlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
            String serviceName = params.get("serviceName");
            
            Map<String, ServiceInstance> instances = registry.get(serviceName);
            if (instances == null || instances.isEmpty()) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            
            ServiceInstance instance = instances.values().iterator().next();
            String url = "http://" + instance.host + ":" + instance.port;
            
            exchange.sendResponseHeaders(200, url.length());
            OutputStream os = exchange.getResponseBody();
            os.write(url.getBytes());
            os.close();
        }
    }
    
    private static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new ConcurrentHashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    params.put(pair[0], pair[1]);
                }
            }
        }
        return params;
    }
    
    private static void startEvictionThread() {
        Thread evictionThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(EVICTION_INTERVAL_MS);
                    long now = System.currentTimeMillis();
                    registry.values().forEach(instances -> 
                        instances.entrySet().removeIf(entry -> 
                            now - entry.getValue().lastHeartbeat > HEARTBEAT_TIMEOUT_MS
                        )
                    );
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        evictionThread.setDaemon(true);
        evictionThread.start();
    }
}