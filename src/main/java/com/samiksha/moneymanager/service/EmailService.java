package com.samiksha.moneymanager.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.from.email}")
    private String fromEmail;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendEmail(String to, String subject, String body) {

        System.out.println("Before sending email");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);   // ✅ FIX

        Map<String, Object> request = Map.of(
                "sender", Map.of(
                        "name", "Money Manager",
                        "email", fromEmail
                ),
                "to", List.of(
                        Map.of("email", to)
                ),
                "subject", subject,
                "htmlContent", body   // better than textContent
        );

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(request, headers);

        restTemplate.postForEntity(
                "https://api.brevo.com/v3/smtp/email",
                entity,
                String.class
        );

        System.out.println("Email sent successfully");
    }
}