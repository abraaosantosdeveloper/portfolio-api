package com.abraaosantosdeveloper.portfolio.mail;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContactMailService {

    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final TemplateEngine templates;
    private final ResendClient resend;

    public void send(String name, String email, String message) {
        var context = new Context(Locale.forLanguageTag("pt-BR"));
        context.setVariable("name", name);
        context.setVariable("email", email);
        context.setVariable("message", message);
        context.setVariable("receivedAt", ZonedDateTime.now(ZONA).format(FORMATO));

        String html = templates.process("mail/contato", context);
        resend.send(email, "Portfólio: nova mensagem de " + oneLine(name), html);
    }

    private static String oneLine(String text) {
        String clean = text.replaceAll("[\\r\\n]+", " ").trim();
        return clean.length() > 60 ? clean.substring(0, 60) : clean;
    }
}