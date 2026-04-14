package com.notificationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.junit4.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetup;
import com.notificationservice.config.AppConfig;
import com.notificationservice.config.KafkaTestConfig;
import com.notificationservice.config.TestMailConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import jakarta.mail.internet.MimeMessage;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AppConfig.class, TestMailConfig.class, KafkaTestConfig.class})
@WebAppConfiguration
@ActiveProfiles("test")
public class NotificationControllerTest {
    
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(new ServerSetup(3025, null, ServerSetup.PROTOCOL_SMTP));
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }
    
    @Test
    public void testSendCreateNotification() throws Exception {
        String email = "test@example.com";
        
        mockMvc.perform(post("/api/notifications/create")
                .param("email", email))
                .andExpect(status().isOk());
        
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        assertEquals("Welcome! Account Created", messages[0].getSubject());
    }
    
    @Test
    public void testSendDeleteNotification() throws Exception {
        String email = "test@example.com";
        
        mockMvc.perform(post("/api/notifications/delete")
                .param("email", email))
                .andExpect(status().isOk());
        
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        assertEquals("Account Deleted", messages[0].getSubject());
    }
}