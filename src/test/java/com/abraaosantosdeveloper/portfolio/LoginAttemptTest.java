package com.abraaosantosdeveloper.portfolio;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.abraaosantosdeveloper.portfolio.domain.user.User;
import com.abraaosantosdeveloper.portfolio.domain.user.UserRepository;

@SpringBootTest(properties = {
    "app.admin.username=test-admin",
    "app.admin.password-hash=$2y$12$testtesttesttesttesttesttesttesttesttesttesttesttesttes"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class LoginAttemptTest {

    private static final String USUARIO = "test-admin";
    private static final String SENHA = "senha-certa";

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository users;

    @Autowired
    PasswordEncoder encoder;

    @BeforeEach
    void prepararAdmin() {
        users.deleteAll();
        users.save(User.builder()
            .username(USUARIO)
            .passwordHash(encoder.encode(SENHA))
            .build());
    }

    private ResultActions entrar(String ip, String senha) throws Exception {
        return mvc.perform(post("/admin/login")
            .param("username", USUARIO)
            .param("password", senha)
            .with(csrf())
            .with(request -> {
                request.setRemoteAddr(ip);
                return request;
            }));
    }

    @Test
    void loginCorretoVaiParaOPainel() throws Exception {
        entrar("10.1.0.1", SENHA).andExpect(redirectedUrl("/admin/projetos"));
    }

    @Test
    void senhaErradaVoltaParaOLoginComErro() throws Exception {
        entrar("10.1.0.2", "errada").andExpect(redirectedUrl("/admin/login?erro"));
    }

    @Test
    void cincoErrosBloqueiamMesmoComSenhaCerta() throws Exception {
        for (int i = 0; i < 5; i++) {
            entrar("10.1.0.3", "errada").andExpect(redirectedUrl("/admin/login?erro"));
        }
        entrar("10.1.0.3", SENHA).andExpect(redirectedUrl("/admin/login?bloqueado"));
    }

    @Test
    void bloqueioNaoAtingeOutroIp() throws Exception {
        for (int i = 0; i < 5; i++) {
            entrar("10.1.0.4", "errada");
        }
        entrar("10.1.0.4", SENHA).andExpect(redirectedUrl("/admin/login?bloqueado"));

        entrar("10.1.0.5", SENHA).andExpect(redirectedUrl("/admin/projetos"));
    }

    @Test
    void loginCorretoZeraOContador() throws Exception {
        for (int i = 0; i < 4; i++) {
            entrar("10.1.0.6", "errada");
        }
        entrar("10.1.0.6", SENHA).andExpect(redirectedUrl("/admin/projetos"));

        for (int i = 0; i < 4; i++) {
            entrar("10.1.0.6", "errada").andExpect(redirectedUrl("/admin/login?erro"));
        }
    }
}