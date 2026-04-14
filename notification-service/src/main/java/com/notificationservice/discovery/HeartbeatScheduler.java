package com.userservice.discovery;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Периодическая отправка heartbeat в Service Registry.
 * Запускается автоматически при старте Spring контекста.
 */
public class HeartbeatScheduler implements InitializingBean, DisposableBean {
    private final ServiceDiscoveryClient discoveryClient;
    private ScheduledExecutorService scheduler;
    
    public HeartbeatScheduler(ServiceDiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }
    
    @Override
    public void afterPropertiesSet() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        // Регистрация при старте
        discoveryClient.register();
        // Heartbeat каждые 10 секунд
        scheduler.scheduleAtFixedRate(() -> discoveryClient.sendHeartbeat(), 10, 10, TimeUnit.SECONDS);
        System.out.println("[HeartbeatScheduler] Started with 10s interval");
    }
    
    @Override
    public void destroy() {
        if (scheduler != null) {
            discoveryClient.deregister();
            scheduler.shutdown();
            System.out.println("[HeartbeatScheduler] Stopped");
        }
    }
}