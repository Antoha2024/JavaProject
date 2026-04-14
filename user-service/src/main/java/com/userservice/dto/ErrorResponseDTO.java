package com.userservice.dto;

/**
 * DTO для стандартизированного ответа при ошибках.
 * 
 * Обеспечивает единый формат ошибок для всего API.
 * 
 */
public class ErrorResponseDTO {
    private int statusCode;
    private String errorCode;
    private String message;
    private long timestamp;
    
    public ErrorResponseDTO() {
    }
    
    public ErrorResponseDTO(int statusCode, String errorCode, String message) {
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and setters
    public int getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}