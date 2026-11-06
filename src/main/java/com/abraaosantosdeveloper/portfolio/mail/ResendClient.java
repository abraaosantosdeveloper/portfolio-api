package com.abraaosantosdeveloper.portfolio.mail;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ResendClient {

    private final RestClient http;
    private final String apiKey;
    private final String from;
    private final String to;

    public ResendClient(
            @Value("${app.mail.resend-api-key}") String apiKey,
            @Value("${app.mail.from}") String from,
            @Value("${app.mail.to}") String to) {
        this.apiKey = apiKey;
        this.from = from;
        this.to = to;

        var factory = new JdkClientHttpRequestFactory(
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(10));

        this.http = RestClient.builder()
            .baseUrl("https://api.resend.com")
            .requestFactory(factory)
            .defaultHeader(HttpHeaders.USER_AGENT, "portfolio-api")
            .build();
    }

    public void send(String replyTo, String subject, String html) {
        if (apiKey.isBlank() || to.isBlank()) {
            throw new IllegalStateException("RESEND_API_KEY e CONTACT_TO_EMAIL precisam estar configurados");
        }
        var body = Map.of(
            "from", from,
            "to", List.of(to),
            "reply_to", replyTo,
            "subject", subject,
            "html", html);
        try {
            http.post()
                .uri("/emails")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException e) {
            log.error("Resend recusou o envio: HTTP {}", e.getStatusCode().value());
            throw new IllegalStateException("Falha ao enviar e-mail pelo Resend", e);
        }
    }
}