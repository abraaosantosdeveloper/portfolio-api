package com.abraaosantosdeveloper.portfolio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.abraaosantosdeveloper.portfolio.domain.settings.SiteSettings;
import com.abraaosantosdeveloper.portfolio.domain.settings.SiteSettingsRepository;
import com.abraaosantosdeveloper.portfolio.domain.timeline.PhaseKind;
import com.abraaosantosdeveloper.portfolio.domain.timeline.TimelinePhase;
import com.abraaosantosdeveloper.portfolio.domain.timeline.TimelinePhaseRepository;

@SpringBootTest(properties = {
    "app.admin.username=test-admin",
    "app.admin.password-hash=$2y$12$testtesttesttesttesttesttesttesttesttesttesttesttesttes"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@WithMockUser(username = "test-admin")
class TimelineAdminTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    TimelinePhaseRepository phases;

    @Autowired
    SiteSettingsRepository settings;

    TimelinePhase atual;

    @BeforeEach
    void preparar() {
        phases.deleteAll();
        atual = phases.save(TimelinePhase.builder()
            .kind(PhaseKind.WORK)
            .title("Desenvolvedor — Inovação")
            .institution("Empresa")
            .startDate(LocalDate.of(2024, 1, 1))
            .build());
        settings.save(SiteSettings.builder()
            .id(SiteSettings.ID)
            .currentPhaseId(atual.getId())
            .nextPhaseText("Quem sabe na sua empresa?")
            .build());
    }

    @Test
    void telasRenderizam() throws Exception {
        mvc.perform(get("/admin/trajetoria"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Subiu de fase?")))
            .andExpect(content().string(containsString("VOCÊ ESTÁ AQUI")));

        mvc.perform(get("/admin/trajetoria/nova"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Nova fase")));

        mvc.perform(get("/admin/trajetoria/" + atual.getId()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Editar fase")))
            .andExpect(content().string(containsString("Excluir esta fase")));
    }

    @Test
    void novaFaseAtualEncerraAAnterior() throws Exception {
        mvc.perform(post("/admin/trajetoria")
                .param("kind", "WORK")
                .param("title", "Back-end Pleno")
                .param("start", "03/2026")
                .param("ongoing", "true")
                .param("current", "true")
                .param("previousEnd", "02/2026")
                .param("technologies", "Java, Spring Boot")
                .param("highlights", "APIs\nMensageria")
                .with(csrf()))
            .andExpect(redirectedUrl("/admin/trajetoria"));

        var nova = phases.findAll().stream().filter(p -> p.getTitle().equals("Back-end Pleno")).findFirst().orElseThrow();
        assertThat(nova.getStartDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(nova.getEndDate()).isNull();
        assertThat(nova.getTechnologies()).containsExactly("Java", "Spring Boot");
        assertThat(nova.getHighlights()).containsExactly("APIs", "Mensageria");

        assertThat(settings.findById(SiteSettings.ID).orElseThrow().getCurrentPhaseId()).isEqualTo(nova.getId());
        assertThat(phases.findById(atual.getId()).orElseThrow().getEndDate()).isEqualTo(LocalDate.of(2026, 2, 1));
    }

    @Test
    void faseDoHistoricoNaoMudaADestacada() throws Exception {
        mvc.perform(post("/admin/trajetoria")
                .param("kind", "STUDY")
                .param("title", "Curso de Kafka")
                .param("start", "01/2023")
                .param("end", "06/2023")
                .param("ongoing", "false")
                .param("current", "false")
                .with(csrf()))
            .andExpect(redirectedUrl("/admin/trajetoria"));

        assertThat(settings.findById(SiteSettings.ID).orElseThrow().getCurrentPhaseId()).isEqualTo(atual.getId());
        var curso = phases.findAll().stream().filter(p -> p.getTitle().equals("Curso de Kafka")).findFirst().orElseThrow();
        assertThat(curso.getEndDate()).isEqualTo(LocalDate.of(2023, 6, 1));
    }

    @Test
    void dadosInvalidosVoltamParaOPainel() throws Exception {
        mvc.perform(post("/admin/trajetoria")
                .param("kind", "WORK")
                .param("title", "")
                .param("start", "13/2026")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/trajetoria"))
            .andExpect(model().attributeHasFieldErrors("faseForm", "title", "start"));

        assertThat(phases.count()).isEqualTo(1);
    }

    @Test
    void excluirAFaseAtualTiraODestaque() throws Exception {
        mvc.perform(post("/admin/trajetoria/" + atual.getId() + "/excluir").with(csrf()))
            .andExpect(redirectedUrl("/admin/trajetoria"));

        assertThat(phases.count()).isZero();
        assertThat(settings.findById(SiteSettings.ID).orElseThrow().getCurrentPhaseId()).isNull();
    }

    @Test
    void textoDaProximaFase() throws Exception {
        mvc.perform(post("/admin/trajetoria/proxima-fase")
                .param("texto", "  Bora trabalhar juntos?  ")
                .with(csrf()))
            .andExpect(redirectedUrl("/admin/trajetoria"));

        assertThat(settings.findById(SiteSettings.ID).orElseThrow().getNextPhaseText()).isEqualTo("Bora trabalhar juntos?");
    }
}
