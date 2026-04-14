package com.userservice.service;

import com.userservice.dto.UserRequestDTO;
import com.userservice.dto.UserResponseDTO;
import java.util.List;
import java.util.Optional;

public interface UserService {
    
    List<UserResponseDTO> getAllUsers();
    
    Optional<UserResponseDTO> getUserById(Long id);
    
    Optional<UserResponseDTO> getUserByEmail(String email);
    
    UserResponseDTO createUser(UserRequestDTO userRequest);
    
    Optional<UserResponseDTO> updateUser(Long id, UserRequestDTO userRequest);
    
    boolean deleteUser(Long id);
    
    int removeDuplicateUsers();
}