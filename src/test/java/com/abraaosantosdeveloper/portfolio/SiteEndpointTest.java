package com.abraaosantosdeveloper.portfolio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.abraaosantosdeveloper.portfolio.config.CacheConfig;
import com.abraaosantosdeveloper.portfolio.domain.project.Project;
import com.abraaosantosdeveloper.portfolio.domain.project.ProjectImage;
import com.abraaosantosdeveloper.portfolio.domain.project.ProjectRepository;
import com.abraaosantosdeveloper.portfolio.domain.project.ProjectStatus;
import com.abraaosantosdeveloper.portfolio.domain.timeline.PhaseKind;
import com.abraaosantosdeveloper.portfolio.domain.timeline.TimelinePhase;
import com.abraaosantosdeveloper.portfolio.domain.timeline.TimelinePhaseRepository;

@SpringBootTest(properties = {
    "app.admin.username=test-admin",
    "app.admin.password-hash=$2y$12$testtesttesttesttesttesttesttesttesttesttesttesttesttes"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SiteEndpointTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ProjectRepository projects;

    @Autowired
    TimelinePhaseRepository phases;

    @Autowired
    CacheManager caches;

    @BeforeEach
    void limpar() {
        projects.deleteAll();
        phases.deleteAll();
        caches.getCache(CacheConfig.SITE).clear();
    }

    @Test
    void naoVazaRascunhoNemCamposInternos() throws Exception {
        projects.save(Project.builder()
            .slug("publicado").title("Publicado").year(2026)
            .status(ProjectStatus.PRODUCTION).published(true)
            .authorId(new ObjectId().toHexString())
            .images(List.of(new ProjectImage("pasta/abc", "https://img.test/x.png", 800, 600)))
            .build());
        projects.save(Project.builder()
            .slug("rascunho").title("Rascunho").year(2026)
            .status(ProjectStatus.IN_PROGRESS).published(false)
            .build());

        mvc.perform(get("/api/site"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.projects.length()").value(1))
            .andExpect(jsonPath("$.projects[0].slug").value("publicado"))
            .andExpect(jsonPath("$.projects[0].authorId").doesNotExist())
            .andExpect(jsonPath("$.projects[0].published").doesNotExist())
            .andExpect(jsonPath("$.projects[0].images[0].publicId").doesNotExist())
            .andExpect(jsonPath("$.projects[0].images[0].url").value("https://img.test/x.png"));
    }

    @Test
    void devolveStackETrajetoriaComDatasEmTexto() throws Exception {
        phases.save(TimelinePhase.builder()
            .kind(PhaseKind.WORK).title("Dev back-end")
            .startDate(LocalDate.of(2022, 3, 1))
            .build());

        mvc.perform(get("/api/site"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stack").isNotEmpty())
            .andExpect(jsonPath("$.timeline.phases[0].kind").value("WORK"))
            .andExpect(jsonPath("$.timeline.phases[0].startDate").value("2022-03-01"))
            .andExpect(jsonPath("$.timeline.phases[0].endDate").doesNotExist());
    }

    @Test
    void rotasNaoListadasFicamBloqueadas() throws Exception {
        mvc.perform(get("/qualquer-coisa"))
            .andExpect(status().isForbidden());
    }
}