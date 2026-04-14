package com.userservice.aspect;

import com.userservice.dto.UserRequestDTO;
import com.userservice.exception.DuplicateUserException;
import com.userservice.repository.UserRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Аспект для фильтрации дубликатов пользователей при создании.
 * 
 * Перехватывает вызовы метода createUser и проверяет, 
 * существует ли пользователь с указанным email в базе данных.
 * Если дубликат найден, выбрасывается DuplicateUserException.
 * 
 * Использует @Around advice для выполнения проверки до вызова целевого метода.
 * 
 */
@Aspect
@Component
public class DuplicateFilterAspect {
    
    private final UserRepository userRepository;
    
    @Autowired
    public DuplicateFilterAspect(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Pointcut("execution(* com.userservice.service.UserService.createUser(..)) && args(userRequest)")
    public void createUserPointcut(UserRequestDTO userRequest) {}
    
    @Around(value = "createUserPointcut(userRequest)", argNames = "joinPoint,userRequest")
    public Object filterDuplicateUser(ProceedingJoinPoint joinPoint, UserRequestDTO userRequest) throws Throwable {
        // Проверяем, существует ли пользователь с таким email
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            // Используем кастомное исключение вместо RuntimeException
            throw new DuplicateUserException(userRequest.getEmail());
        }
        
        return joinPoint.proceed();
    }
}