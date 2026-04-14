package com.notificationservice.service;

import com.notificationservice.dto.UserEventDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Реализация сервиса для отправки email-уведомлений
 */
@Service
public class EmailServiceImpl implements EmailService {

    @Value("${mail.from}")
    private String fromEmail;

    @Value("${mail.subject.create}")
    private String createSubject;

    @Value("${mail.subject.delete}")
    private String deleteSubject;

    @Value("${mail.text.create}")
    private String createText;

    @Value("${mail.text.delete}")
    private String deleteText;

    private final JavaMailSender mailSender;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(UserEventDTO event, String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);

        if (event.getOperation() == UserEventDTO.OperationType.CREATE) {
            message.setSubject(createSubject);
            message.setText(String.format(createText, toEmail));
        } else {
            message.setSubject(deleteSubject);
            message.setText(String.format(deleteText, toEmail));
        }

        mailSender.send(message);
    }
}