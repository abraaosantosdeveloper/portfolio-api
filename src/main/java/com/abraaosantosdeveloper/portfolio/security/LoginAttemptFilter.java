package com.abraaosantosdeveloper.portfolio.security;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginAttemptFilter extends OncePerRequestFilter {

    private final LoginAttemptService attempts;

    public LoginAttemptFilter(LoginAttemptService attempts) {
        this.attempts = attempts;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        boolean envioDoLogin = "POST".equals(request.getMethod())
            && request.getRequestURI().equals(request.getContextPath() + "/admin/login");
        return !envioDoLogin;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (attempts.isBlocked(request.getParameter("username"), request.getRemoteAddr())) {
            response.sendRedirect(request.getContextPath() + "/admin/login?bloqueado");
            return;
        }
        chain.doFilter(request, response);
    }
}