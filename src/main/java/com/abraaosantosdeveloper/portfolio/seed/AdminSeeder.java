package com.abraaosantosdeveloper.portfolio.seed;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.abraaosantosdeveloper.portfolio.domain.user.User;
import com.abraaosantosdeveloper.portfolio.domain.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@Order(2) // depois das migrations (índice único de username já existe)
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {
private final UserRepository users;
    private final AdminProperties admin;

    @Override
    public void run(ApplicationArguments args) {
        if (admin.passwordHash() == null || !admin.passwordHash().startsWith("$2")) {
            throw new IllegalStateException(
                "APP_ADMIN_PASSWORD_HASH precisa ser um hash bcrypt ($2a$, $2b$ ou $2y$)");
        }
        users.findByUserName(admin.username()).ifPresentOrElse(user -> {
            if (!user.getPasswordHash().equals(admin.passwordHash())) {
                user.setPasswordHash(admin.passwordHash());
                users.save(user);
                log.info("Senha do admin atualizada");
            }
        }, () -> {
            users.save(User.builder()
                .userName(admin.username())
                .passwordHash(admin.passwordHash())
                .build());
            log.info("Admin '{}' criado", admin.username());
        });
    }
}
