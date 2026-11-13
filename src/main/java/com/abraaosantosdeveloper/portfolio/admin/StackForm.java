package com.abraaosantosdeveloper.portfolio.admin;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Tudo o que a tela de Stack envia de uma vez.
 * Os campos chegam como categorias[0].id, categorias[0].title, categorias[0].items (repetido)...
 * A posição na lista é a ordem no site.
 */
@Getter
@Setter
public class StackForm {

    private List<Categoria> categorias = new ArrayList<>();

    @Getter
    @Setter
    public static class Categoria {

        /** Vazio = categoria nova. */
        private String id;

        /** Vazio numa categoria que já existe = apagar a categoria. */
        private String title;

        /** Nomes das tecnologias. Chega null quando a categoria ficou sem itens. */
        private List<String> items = new ArrayList<>();
    }
}
