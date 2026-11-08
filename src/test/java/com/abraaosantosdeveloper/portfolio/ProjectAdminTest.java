package com.abraaosantosdeveloper.portfolio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.abraaosantosdeveloper.portfolio.config.CacheConfig;
import com.abraaosantosdeveloper.portfolio.domain.project.ProjectRepository;
import com.abraaosantosdeveloper.portfolio.domain.user.User;
import com.abraaosantosdeveloper.portfolio.domain.user.UserRepository;

@SpringBootTest(properties = {
        "app.admin.username=test-admin",
        "app.admin.password-hash=$2y$12$testtesttesttesttesttesttesttesttesttesttesttesttesttes"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@WithMockUser(username = "test-admin")
class ProjectAdminTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ProjectRepository projects;

    @Autowired
    UserRepository users;

    @Autowired
    CacheManager caches;

    @BeforeEach
    void limpar() {
        projects.deleteAll();
        caches.getCache(CacheConfig.SITE).clear();
        if (users.findByUsername("test-admin").isEmpty()) {
            users.save(User.builder().username("test-admin").passwordHash("$2y$12$x").build());
        }
    }

    private ResultActions salvar(String title, boolean publicado) throws Exception {
        var request = post("/admin/projetos")
                .param("title", title)
                .param("year", "2026")
                .param("status", "PRODUCTION")
                .param("description", "Descrição curta do projeto")
                .param("icon", "code")
                .param("color", "menta-2")
                .param("tags", "Java, Spring Boot")
                .with(csrf());
        if (publicado) {
            request.param("published", "true");
        }
        return mvc.perform(request);
    }

    @Test
    void criaProjetoComSlugGeradoEAutorDoServidor() throws Exception {
        mvc.perform(post("/admin/projetos")
                .param("title", "Sistema de Chamados Ágil")
                .param("year", "2026")
                .param("status", "PRODUCTION")
                .param("description", "Descrição curta")
                .param("icon", "code")
                .param("color", "menta-2")
                .param("tags", "Java, Spring Boot")
                .param("slug", "slug-forjado")
                .param("authorId", "000000000000000000000001")
                .with(csrf()))
                .andExpect(redirectedUrl("/admin/projetos?salvo"));

        var project = projects.findBySlug("sistema-de-chamados-agil").orElseThrow();
        var autorEsperado = users.findByUsername("test-admin").orElseThrow().getId();

        assertThat(project.getAuthorId()).isEqualTo(autorEsperado);
        assertThat(project.getSlug()).isNotEqualTo("slug-forjado");
        assertThat(project.isPublished()).isFalse();
        assertThat(project.getTags()).containsExactly("Java", "Spring Boot");
        assertThat(projects.findBySlug("slug-forjado")).isEmpty();
    }

    @Test
    void tituloRepetidoGanhaSufixoNoSlug() throws Exception {
        salvar("Meu Projeto", false).andExpect(redirectedUrl("/admin/projetos?salvo"));
        salvar("Meu Projeto", false).andExpect(redirectedUrl("/admin/projetos?salvo"));

        assertThat(projects.findBySlug("meu-projeto")).isPresent();
        assertThat(projects.findBySlug("meu-projeto-2")).isPresent();
    }

    @Test
    void rascunhoNaoApareceNaApiEPublicarLimpaOCache() throws Exception {
        salvar("Rascunho", false);

        // Esta chamada guarda a resposta no cache, ainda sem projetos publicados
        mvc.perform(get("/api/site"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projects.length()").value(0));

        // Salvar um publicado precisa esvaziar o cache, ou a próxima chamada viria
        // velha
        salvar("Publicado", true);

        mvc.perform(get("/api/site"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projects.length()").value(1))
                .andExpect(jsonPath("$.projects[0].slug").value("publicado"));
    }

    @Test
    void dadosInvalidosNaoGravamEVoltamParaOFormulario() throws Exception {
        mvc.perform(post("/admin/projetos")
                .param("title", "")
                .param("year", "2026")
                .param("status", "PRODUCTION")
                .param("description", "x".repeat(121))
                .param("icon", "nao-existe")
                .param("color", "menta-2")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/projetos/form"))
                .andExpect(model().attributeHasFieldErrors("form", "title", "description", "iconValid"));

        assertThat(projects.count()).isZero();
    }

    @Test
    void editarNaoMudaOSlug() throws Exception {
        salvar("Título Original", false);
        var id = projects.findBySlug("titulo-original").orElseThrow().getId();

        mvc.perform(post("/admin/projetos/" + id)
                .param("title", "Título Novo")
                .param("year", "2026")
                .param("status", "ARCHIVED")
                .param("description", "Outra descrição")
                .param("icon", "code")
                .param("color", "menta-2")
                .with(csrf()))
                .andExpect(redirectedUrl("/admin/projetos?salvo"));

        var project = projects.findById(id).orElseThrow();
        assertThat(project.getTitle()).isEqualTo("Título Novo");
        assertThat(project.getSlug()).isEqualTo("titulo-original");
    }

    @Test
    @WithAnonymousUser
    void semLoginVaiParaATelaDeLogin() throws Exception {
        mvc.perform(get("/admin/projetos"))
                .andExpect(redirectedUrl("/admin/login"));
    }
}
