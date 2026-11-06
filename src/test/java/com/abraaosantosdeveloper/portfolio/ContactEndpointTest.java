package com.abraaosantosdeveloper.portfolio;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.abraaosantosdeveloper.portfolio.mail.ResendClient;

@SpringBootTest(properties = {
    "app.admin.username=test-admin",
    "app.admin.password-hash=$2y$12$testtesttesttesttesttesttesttesttesttesttesttesttesttes"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ContactEndpointTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ResendClient resend;

    private ResultActions enviar(String ip, String name, String email, String message, String website)
            throws Exception {
        String json = """
            {"name":"%s","email":"%s","message":"%s","website":"%s"}
            """.formatted(name, email, message, website);
        return mvc.perform(post("/api/contact")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json.getBytes(UTF_8))
            .with(request -> {
                request.setRemoteAddr(ip);
                return request;
            }));
    }

    @Test
    void mensagemValidaEnviaEmailEscapandoOHtml() throws Exception {
        enviar("10.0.0.1", "José", "jose@exemplo.com", "Olá <b>oi</b>", "")
            .andExpect(status().isAccepted());

        var html = ArgumentCaptor.forClass(String.class);
        verify(resend).send(eq("jose@exemplo.com"), anyString(), html.capture());
        assertThat(html.getValue())
            .contains("José")
            .contains("&lt;b&gt;oi&lt;/b&gt;")
            .doesNotContain("<b>oi</b>");
    }

    @Test
    void honeypotPreenchidoRespondeAceitoENaoEnvia() throws Exception {
        enviar("10.0.0.2", "Robo", "robo@exemplo.com", "spam", "http://spam.com")
            .andExpect(status().isAccepted());

        verifyNoInteractions(resend);
    }

    @Test
    void dadosInvalidosRespondem400ENaoEnviam() throws Exception {
        enviar("10.0.0.3", "Ana", "isso-nao-e-email", "oi", "").andExpect(status().isBadRequest());
        enviar("10.0.0.3", "", "ana@exemplo.com", "oi", "").andExpect(status().isBadRequest());
        enviar("10.0.0.3", "Ana", "ana@exemplo.com", "a".repeat(2001), "").andExpect(status().isBadRequest());

        verifyNoInteractions(resend);
    }

    @Test
    void quartoEnvioDoMesmoIpRecebe429() throws Exception {
        for (int i = 0; i < 3; i++) {
            enviar("10.0.0.4", "Ana", "ana@exemplo.com", "mensagem " + i, "")
                .andExpect(status().isAccepted());
        }
        enviar("10.0.0.4", "Ana", "ana@exemplo.com", "mensagem 4", "")
            .andExpect(status().isTooManyRequests())
            .andExpect(header().string("Retry-After", "600"));

        verify(resend, times(3)).send(anyString(), anyString(), anyString());
    }

    @Test
    void falhaNoResendRespondeBadGateway() throws Exception {
        doThrow(new IllegalStateException("falhou")).when(resend).send(anyString(), anyString(), anyString());

        enviar("10.0.0.5", "Ana", "ana@exemplo.com", "oi", "")
            .andExpect(status().isBadGateway());
    }
}