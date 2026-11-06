package com.abraaosantosdeveloper.portfolio.api;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abraaosantosdeveloper.portfolio.mail.ContactMailService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContactController {

    private static final int MAX_POR_JANELA = 3;
    private static final Duration JANELA = Duration.ofMinutes(10);

    private final ContactMailService mail;

    private final Cache<String, AtomicInteger> tentativas = Caffeine.newBuilder()
        .expireAfterWrite(JANELA)
        .maximumSize(10_000)
        .build();

    @PostMapping("/contact")
    public ResponseEntity<Void> contact(@Valid @RequestBody ContactRequest request,
                                        HttpServletRequest http) {
        // Honeypot: robô preenche o campo escondido. Finge sucesso e não envia nada.
        if (request.website() != null && !request.website().isBlank()) {
            return ResponseEntity.accepted().build();
        }

        String ip = http.getRemoteAddr();
        int usadas = tentativas.get(ip, k -> new AtomicInteger()).incrementAndGet();
        if (usadas > MAX_POR_JANELA) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(JANELA.toSeconds()))
                .build();
        }

        try {
            mail.send(request.name(), request.email(), request.message());
        } catch (RuntimeException e) {
            log.error("Falha ao enviar mensagem de contato: {}", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
        return ResponseEntity.accepted().build();
    }
}