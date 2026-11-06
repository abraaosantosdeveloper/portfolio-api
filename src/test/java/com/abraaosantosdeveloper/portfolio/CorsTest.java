package com.abraaosantosdeveloper.portfolio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "app.admin.username=test-admin",
    "app.admin.password-hash=$2y$12$testtesttesttesttesttesttesttesttesttesttesttesttesttes",
    "app.cors.allowed-origins=http://localhost:5173,https://abraaosantosdeveloper.github.io"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class CorsTest {

    @Autowired
    MockMvc mvc;

    @Test
    void origemLiberadaRecebeOCabecalho() throws Exception {
        mvc.perform(get("/api/site").header("Origin", "http://localhost:5173"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void origemNaoListadaEBloqueada() throws Exception {
        mvc.perform(get("/api/site").header("Origin", "https://outro-site.com"))
            .andExpect(status().isForbidden());
    }

    @Test
    void preVooDoPostEAceitoParaOFront() throws Exception {
        mvc.perform(options("/api/contact")
                .header("Origin", "https://abraaosantosdeveloper.github.io")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin",
                "https://abraaosantosdeveloper.github.io"));
    }
}