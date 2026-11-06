package com.abraaosantosdeveloper.portfolio.security;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginAttemptListener {

    private final LoginAttemptService attempts;

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        Authentication auth = event.getAuthentication();
        attempts.recordFailure(auth.getName(), ipOf(auth));
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        attempts.recordSuccess(auth.getName(), ipOf(auth));
    }

    private static String ipOf(Authentication auth) {
        return auth.getDetails() instanceof WebAuthenticationDetails details
            ? details.getRemoteAddress()
            : "desconhecido";
    }
}
