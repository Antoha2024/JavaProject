package com.userservice.exception;

/**
 * Исключение при попытке найти несуществующего пользователя.
 * 
 * Является RuntimeException, поэтому не требует обязательной обработки.
 * Глобальный обработчик GlobalExceptionHandler перехватит его и вернёт HTTP 404.
 * 
 */
public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(Long id) {
        super("User with id " + id + " not found");
    }
    
    public UserNotFoundException(String email) {
        super("User with email " + email + " not found");
    }
}