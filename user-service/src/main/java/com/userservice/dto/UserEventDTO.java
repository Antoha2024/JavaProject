package com.userservice.dto;

/**
 * DTO для отправки событий о пользователе в Kafka
 * Содержит информацию об операции (CREATE/DELETE) и email пользователя
 */
public class UserEventDTO {
    
    public enum OperationType {
        CREATE, DELETE
    }
    
    private OperationType operation;
    private String email;
    
    public UserEventDTO() {
    }
    
    public UserEventDTO(OperationType operation, String email) {
        this.operation = operation;
        this.email = email;
    }
    
    public OperationType getOperation() {
        return operation;
    }
    
    public void setOperation(OperationType operation) {
        this.operation = operation;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}