package com.userservice.circuitbreaker;

import java.time.Instant;
import java.util.function.Supplier;

/**
 * Ручная реализация Circuit Breaker паттерна.
 * 
 * Состояния:
 * - CLOSED: запросы проходят, ошибки увеличивают счётчик
 * - OPEN: запросы блокируются, возвращается fallback
 * - HALF_OPEN: пробный запрос для проверки восстановления
 * 
 * Аналог Resilience4J.
 * 
 * @author Antoha2024
 */
public class CircuitBreaker {
    private State state = State.CLOSED;
    private int failureCount = 0;
    private Instant lastFailureTime;
    
    private final int failureThreshold;
    private final long timeoutMs;
    private final int halfOpenMaxAttempts;
    private int halfOpenAttempts = 0;
    
    public enum State { CLOSED, OPEN, HALF_OPEN }
    
    /**
     * @param failureThreshold количество ошибок для размыкания
     * @param timeoutMs время в OPEN состоянии перед переходом в HALF_OPEN
     * @param halfOpenMaxAttempts количество пробных запросов в HALF_OPEN
     */
    public CircuitBreaker(int failureThreshold, long timeoutMs, int halfOpenMaxAttempts) {
        this.failureThreshold = failureThreshold;
        this.timeoutMs = timeoutMs;
        this.halfOpenMaxAttempts = halfOpenMaxAttempts;
    }
    
    public synchronized <T> T execute(Supplier<T> supplier, Supplier<T> fallback) {
        if (state == State.OPEN) {
            if (Instant.now().isAfter(lastFailureTime.plusMillis(timeoutMs))) {
                System.out.println("[CircuitBreaker] OPEN -> HALF_OPEN after " + timeoutMs + "ms");
                state = State.HALF_OPEN;
                halfOpenAttempts = 0;
            } else {
                System.out.println("[CircuitBreaker] OPEN: executing fallback");
                return fallback.get();
            }
        }
        
        try {
            T result = supplier.get();
            if (state == State.HALF_OPEN) {
                halfOpenAttempts++;
                if (halfOpenAttempts >= halfOpenMaxAttempts) {
                    System.out.println("[CircuitBreaker] HALF_OPEN -> CLOSED (success threshold reached)");
                    state = State.CLOSED;
                    failureCount = 0;
                }
            } else {
                failureCount = 0;
            }
            return result;
        } catch (Exception e) {
            System.err.println("[CircuitBreaker] Execution failed: " + e.getMessage());
            recordFailure();
            return fallback.get();
        }
    }
    
    private void recordFailure() {
        failureCount++;
        lastFailureTime = Instant.now();
        
        if (state == State.CLOSED && failureCount >= failureThreshold) {
            System.out.println("[CircuitBreaker] CLOSED -> OPEN after " + failureCount + " failures");
            state = State.OPEN;
        } else if (state == State.HALF_OPEN) {
            System.out.println("[CircuitBreaker] HALF_OPEN -> OPEN (failure in half-open state)");
            state = State.OPEN;
            halfOpenAttempts = 0;
        }
    }
    
    public State getState() { return state; }
}