package com.userservice.exception;

/**
 * Исключение при попытке создать пользователя с существующим email.
 * 
 * Является RuntimeException, поэтому не требует обязательной обработки.
 * Глобальный обработчик GlobalExceptionHandler перехватит его и вернёт HTTP 409.
 * 
 */
public class DuplicateUserException extends RuntimeException {
    
    public DuplicateUserException(String email) {
        super("User with email " + email + " already exists");
    }
}