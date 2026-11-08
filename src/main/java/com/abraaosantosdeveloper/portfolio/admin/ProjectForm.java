package com.abraaosantosdeveloper.portfolio.admin;

import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.abraaosantosdeveloper.portfolio.domain.project.Project;
import com.abraaosantosdeveloper.portfolio.domain.project.ProjectCatalog;
import com.abraaosantosdeveloper.portfolio.domain.project.ProjectStatus;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProjectForm {

    private static final int MAX_TAGS = 8;
    private static final int MAX_TAG_LENGTH = 30;
    private static final String URL_REGEX = "^$|^https?://\\S+$";

    @NotBlank(message = "Informe o nome do projeto")
    @Size(max = 100, message = "No máximo 100 caracteres")
    private String title;

    @NotNull(message = "Informe o ano")
    @Min(value = 2000, message = "Ano inválido")
    @Max(value = 2100, message = "Ano inválido")
    private Integer year;

    @NotNull(message = "Escolha o status")
    private ProjectStatus status;

    @NotBlank(message = "Escreva a descrição curta")
    @Size(max = 120, message = "No máximo 120 caracteres")
    private String description;

    @Size(max = 2000, message = "No máximo 2000 caracteres")
    private String problem;

    @Size(max = 2000, message = "No máximo 2000 caracteres")
    private String solution;

    private String icon = ProjectCatalog.DEFAULT_ICON;

    private String color = ProjectCatalog.DEFAULT_COLOR;

    @Size(max = 300, message = "Texto das tags muito longo")
    private String tags; // separadas por vírgula

    @Size(max = 300, message = "No máximo 300 caracteres")
    @Pattern(regexp = URL_REGEX, message = "Use um endereço que comece com http:// ou https://")
    private String repoUrl;

    @Size(max = 300, message = "No máximo 300 caracteres")
    @Pattern(regexp = URL_REGEX, message = "Use um endereço que comece com http:// ou https://")
    private String liveUrl;

    private boolean published;

    @AssertTrue(message = "Escolha um ícone da lista")
    public boolean isIconValid() {
        return ProjectCatalog.isValidIcon(icon);
    }

    @AssertTrue(message = "Escolha uma cor da lista")
    public boolean isColorValid() {
        return ProjectCatalog.isValidColor(color);
    }

    @AssertTrue(message = "Use até 8 tags, com até 30 caracteres cada")
    public boolean isTagsValid() {
        List<String> list = tagList();
        return list.size() <= MAX_TAGS && list.stream().allMatch(t -> t.length() <= MAX_TAG_LENGTH);
    }

    /** Tags já limpas: sem espaços nas pontas, sem vazias e sem repetidas. */
    public List<String> tagList() {
        if (tags == null || tags.isBlank()) {
            return new ArrayList<>();
        }
        var unique = new LinkedHashSet<String>();
        for (String part : tags.split(",")) {
            String tag = part.trim();
            if (!tag.isEmpty()) {
                unique.add(tag);
            }
        }
        return new ArrayList<>(unique);
    }

    /** Formulário de "Novo projeto": começa como rascunho, em andamento, no ano atual. */
    public static ProjectForm empty() {
        var form = new ProjectForm();
        form.setYear(Year.now().getValue());
        form.setStatus(ProjectStatus.IN_PROGRESS);
        return form;
    }

    /** Preenche o formulário a partir de um projeto salvo (tela de edição). */
    public static ProjectForm from(Project project) {
        var form = new ProjectForm();
        form.setTitle(project.getTitle());
        form.setYear(project.getYear());
        form.setStatus(project.getStatus());
        form.setDescription(project.getDescription());
        form.setProblem(project.getProblem());
        form.setSolution(project.getSolution());
        form.setIcon(project.getIcon() != null ? project.getIcon() : ProjectCatalog.DEFAULT_ICON);
        form.setColor(project.getColor() != null ? project.getColor() : ProjectCatalog.DEFAULT_COLOR);
        form.setTags(project.getTags() == null ? "" : String.join(", ", project.getTags()));
        form.setRepoUrl(project.getRepoUrl());
        form.setLiveUrl(project.getLiveUrl());
        form.setPublished(project.isPublished());
        return form;
    }

    /** Copia os campos editáveis para a entidade. Slug, imagens e autor não passam por aqui. */
    public void applyTo(Project project) {
        project.setTitle(title.trim());
        project.setYear(year);
        project.setStatus(status);
        project.setDescription(description.trim());
        project.setProblem(blankToNull(problem));
        project.setSolution(blankToNull(solution));
        project.setIcon(icon);
        project.setColor(color);
        project.setTags(tagList());
        project.setRepoUrl(blankToNull(repoUrl));
        project.setLiveUrl(blankToNull(liveUrl));
        project.setPublished(published);
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }
}