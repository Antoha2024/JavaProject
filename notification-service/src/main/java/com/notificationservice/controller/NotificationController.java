package com.notificationservice.controller;

import com.notificationservice.dto.UserEventDTO;
import com.notificationservice.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    private final EmailService emailService;
    
    @Autowired
    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }
    
    @PostMapping("/create")
    public ResponseEntity<String> sendCreateNotification(@RequestParam String email) {
        UserEventDTO event = new UserEventDTO(UserEventDTO.OperationType.CREATE, email);
        emailService.sendEmail(event, email);
        return ResponseEntity.ok("Create notification sent to " + email);
    }
    
    @PostMapping("/delete")
    public ResponseEntity<String> sendDeleteNotification(@RequestParam String email) {
        UserEventDTO event = new UserEventDTO(UserEventDTO.OperationType.DELETE, email);
        emailService.sendEmail(event, email);
        return ResponseEntity.ok("Delete notification sent to " + email);
    }
}