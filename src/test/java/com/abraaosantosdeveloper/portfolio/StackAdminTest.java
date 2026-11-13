package com.abraaosantosdeveloper.portfolio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.abraaosantosdeveloper.portfolio.domain.stack.StackCategory;
import com.abraaosantosdeveloper.portfolio.domain.stack.StackCategoryRepository;
import com.abraaosantosdeveloper.portfolio.domain.stack.StackItem;

@SpringBootTest(properties = {
    "app.admin.username=test-admin",
    "app.admin.password-hash=$2y$12$testtesttesttesttesttesttesttesttesttesttesttesttesttes"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@WithMockUser(username = "test-admin")
class StackAdminTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    StackCategoryRepository categories;

    StackCategory linguagens;
    StackCategory dados;

    @BeforeEach
    void preparar() {
        categories.deleteAll();
        linguagens = categories.save(StackCategory.builder()
            .title("Linguagens").order(1)
            .items(List.of(new StackItem("Java", "Desde a faculdade", LocalDate.of(2022, 3, 1)),
                           new StackItem("Python", null, null)))
            .build());
        dados = categories.save(StackCategory.builder()
            .title("Dados").order(2)
            .items(List.of(new StackItem("MySQL", null, null)))
            .build());
    }

    @Test
    void telaRenderizaComAsCategorias() throws Exception {
        mvc.perform(get("/admin/stack").param("salvo", ""))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Linguagens")))
            .andExpect(content().string(containsString("Python")))
            .andExpect(content().string(containsString("Stack salva.")));
    }

    @Test
    void salvaTudoDeUmaVez() throws Exception {
        mvc.perform(post("/admin/stack")
                // 1ª: Dados renomeada e com item novo
                .param("categorias[0].id", dados.getId())
                .param("categorias[0].title", "Bancos de dados")
                .param("categorias[0].items", "MySQL", "MongoDB")
                // 2ª: Linguagens sem o Python
                .param("categorias[1].id", linguagens.getId())
                .param("categorias[1].title", "Linguagens")
                .param("categorias[1].items", "Java")
                // 3ª: categoria nova
                .param("categorias[2].id", "")
                .param("categorias[2].title", "Testes")
                .param("categorias[2].items", "JUnit", "junit", "  ")
                .with(csrf()))
            .andExpect(redirectedUrl("/admin/stack?salvo"));

        var lista = categories.findAllByOrderByOrderAsc();
        assertThat(lista).extracting(StackCategory::getTitle)
            .containsExactly("Bancos de dados", "Linguagens", "Testes");
        assertThat(lista.get(0).getItems()).extracting(StackItem::name).containsExactly("MySQL", "MongoDB");
        assertThat(lista.get(2).getItems()).extracting(StackItem::name).containsExactly("JUnit");

        // O item que ficou mantém descrição e data
        var java = lista.get(1).getItems().get(0);
        assertThat(java.name()).isEqualTo("Java");
        assertThat(java.description()).isEqualTo("Desde a faculdade");
        assertThat(java.since()).isEqualTo(LocalDate.of(2022, 3, 1));
    }

    @Test
    void nomeVazioApagaACategoria() throws Exception {
        mvc.perform(post("/admin/stack")
                .param("categorias[0].id", linguagens.getId())
                .param("categorias[0].title", "Linguagens")
                .param("categorias[0].items", "Java")
                .param("categorias[1].id", dados.getId())
                .param("categorias[1].title", "")
                .with(csrf()))
            .andExpect(redirectedUrl("/admin/stack?salvo"));

        assertThat(categories.findAll()).extracting(StackCategory::getTitle).containsExactly("Linguagens");
    }

    @Test
    void categoriaNovaSemNomeComItensERecusada() throws Exception {
        mvc.perform(post("/admin/stack")
                .param("categorias[0].id", "")
                .param("categorias[0].title", "")
                .param("categorias[0].items", "Rust")
                .with(csrf()))
            .andExpect(redirectedUrl("/admin/stack"))
            .andExpect(flash().attribute("erro", "Dê um nome à categoria nova."));

        assertThat(categories.count()).isEqualTo(2);
    }
}
