package com.abraaosantosdeveloper.portfolio.security;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Service
public class LoginAttemptService {

    private static final int MAX_FALHAS = 5;
    private static final Duration JANELA = Duration.ofMinutes(15);

    private final Cache<String, AtomicInteger> falhas = Caffeine.newBuilder()
        .expireAfterWrite(JANELA)
        .maximumSize(10_000)
        .build();

    public boolean isBlocked(String username, String ip) {
        AtomicInteger contador = falhas.getIfPresent(chave(username, ip));
        return contador != null && contador.get() >= MAX_FALHAS;
    }

    public void recordFailure(String username, String ip) {
        falhas.get(chave(username, ip), k -> new AtomicInteger()).incrementAndGet();
    }

    public void recordSuccess(String username, String ip) {
        falhas.invalidate(chave(username, ip));
    }

    private static String chave(String username, String ip) {
        String nome = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        return nome + "|" + ip;
    }
}