package com.abraaosantosdeveloper.portfolio.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(String username, String passwordHash) {}
