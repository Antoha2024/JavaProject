package com.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userservice.config.AppConfig;
import com.userservice.config.TestConfig;
import com.userservice.dto.UserRequestDTO;
import com.userservice.dto.UserResponseDTO;
import com.userservice.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для UserController с использованием MockMvc.
 * 
 * Тестирует все REST эндпоинты контроллера:
 * - GET /api/users - получение всех пользователей
 * - GET /api/users/{id} - получение пользователя по ID
 * - GET /api/users/email/{email} - получение по email
 * - POST /api/users - создание пользователя
 * - PUT /api/users/{id} - обновление пользователя
 * - DELETE /api/users/{id} - удаление пользователя
 * - POST /api/users/remove-duplicates - удаление дубликатов
 * 
 * Использует @MockBean для мокирования UserService
 * и Testcontainers для реальной PostgreSQL базы данных.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AppConfig.class, TestConfig.class})
@WebAppConfiguration
@ActiveProfiles("test")
public class UserControllerTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @MockBean
    private UserService userService;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }
    
    @Test
    public void testGetAllUsers() throws Exception {
        UserResponseDTO user1 = new UserResponseDTO();
        user1.setId(1L);
        user1.setEmail("john@example.com");
        user1.setFirstName("John");
        user1.setLastName("Doe");
        
        UserResponseDTO user2 = new UserResponseDTO();
        user2.setId(2L);
        user2.setEmail("jane@example.com");
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        
        when(userService.getAllUsers()).thenReturn(Arrays.asList(user1, user2));
        
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("john@example.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].email").value("jane@example.com"));
    }
    
    @Test
    public void testGetUserById_Success() throws Exception {
        UserResponseDTO user = new UserResponseDTO();
        user.setId(1L);
        user.setEmail("john@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"));
    }
    
    @Test
    public void testGetUserById_NotFound() throws Exception {
        when(userService.getUserById(999L)).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    public void testCreateUser_Success() throws Exception {
        UserRequestDTO request = new UserRequestDTO();
        request.setEmail("new@example.com");
        request.setFirstName("New");
        request.setLastName("User");
        request.setAge(25);
        
        UserResponseDTO response = new UserResponseDTO();
        response.setId(3L);
        response.setEmail("new@example.com");
        response.setFirstName("New");
        response.setLastName("User");
        response.setAge(25);
        
        when(userService.createUser(any(UserRequestDTO.class))).thenReturn(response);
        
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }
    
    @Test
    public void testCreateUser_InvalidData() throws Exception {
        UserRequestDTO request = new UserRequestDTO();
        request.setEmail("invalid-email");
        request.setFirstName("");
        
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    public void testUpdateUser_Success() throws Exception {
        UserRequestDTO request = new UserRequestDTO();
        request.setEmail("updated@example.com");
        request.setFirstName("Updated");
        request.setLastName("User");
        request.setAge(30);
        
        UserResponseDTO response = new UserResponseDTO();
        response.setId(1L);
        response.setEmail("updated@example.com");
        response.setFirstName("Updated");
        response.setLastName("User");
        response.setAge(30);
        
        when(userService.updateUser(eq(1L), any(UserRequestDTO.class))).thenReturn(Optional.of(response));
        
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }
    
    @Test
    public void testUpdateUser_NotFound() throws Exception {
        UserRequestDTO request = new UserRequestDTO();
        request.setEmail("test@example.com");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setAge(30);
        
        when(userService.updateUser(eq(999L), any(UserRequestDTO.class))).thenReturn(Optional.empty());
        
        mockMvc.perform(put("/api/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    public void testDeleteUser_Success() throws Exception {
        when(userService.deleteUser(1L)).thenReturn(true);
        
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
    
    @Test
    public void testDeleteUser_NotFound() throws Exception {
        when(userService.deleteUser(999L)).thenReturn(false);
        
        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    public void testRemoveDuplicateUsers() throws Exception {
        when(userService.removeDuplicateUsers()).thenReturn(5);
        
        mockMvc.perform(post("/api/users/remove-duplicates"))
                .andExpect(status().isOk())
                .andExpect(content().string("Removed 5 duplicate users"));
    }
}