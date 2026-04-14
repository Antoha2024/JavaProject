package com.notificationservice.service;

import com.notificationservice.dto.UserEventDTO;

/**
 * Интерфейс для отправки email-уведомлений
 */
public interface EmailService {

    /**
     * Отправляет email на основе события о пользователе
     * @param event событие (CREATE или DELETE)
     * @param toEmail email получателя
     */
    void sendEmail(UserEventDTO event, String toEmail);
}