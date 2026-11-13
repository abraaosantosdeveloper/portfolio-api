package com.abraaosantosdeveloper.portfolio.admin;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.abraaosantosdeveloper.portfolio.domain.project.ProjectCatalog;
import com.abraaosantosdeveloper.portfolio.domain.project.ProjectStatus;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ProjectAdminController {

    private static final String LISTA = "admin/projetos/lista";
    private static final String FORM = "admin/projetos/form";
    private static final String VOLTAR_PARA_LISTA = "redirect:/admin/projetos?salvo";

    private final ProjectAdminService service;

    // ---------- Dados que o formulário sempre precisa ----------

    @ModelAttribute("icones")
    public List<ProjectCatalog.Icon> icones() {
        return ProjectCatalog.ICONS;
    }

    @ModelAttribute("coresPorFamilia")
    public Map<String, List<ProjectCatalog.Color>> coresPorFamilia() {
        return ProjectCatalog.colorsByFamily();
    }

    @ModelAttribute("statuses")
    public ProjectStatus[] statuses() {
        return ProjectStatus.values();
    }

    @ModelAttribute("coresPorId")
    public Map<String, ProjectCatalog.Color> coresPorId() {
        return ProjectCatalog.COLORS.stream()
            .collect(Collectors.toMap(ProjectCatalog.Color::id, Function.identity()));
    }

    @ModelAttribute("iconesPorId")
    public Map<String, ProjectCatalog.Icon> iconesPorId() {
        return ProjectCatalog.ICONS.stream()
            .collect(Collectors.toMap(ProjectCatalog.Icon::id, Function.identity()));
    }

    // ---------- Lista ----------

    @GetMapping("/admin/projetos")
    public String list(Model model) {
        model.addAttribute("projetos", service.list());
        return LISTA;
    }

    // ---------- Novo projeto ----------

    @GetMapping("/admin/projetos/novo")
    public String newForm(Model model) {
        model.addAttribute("form", ProjectForm.empty());
        return FORM;
    }

    @PostMapping("/admin/projetos")
    public String create(@Valid @ModelAttribute("form") ProjectForm form,
                         BindingResult result,
                         Authentication authentication) {
        if (result.hasErrors()) {
            return FORM;
        }
        service.create(form, authentication.getName());
        return VOLTAR_PARA_LISTA;
    }

    // ---------- Editar projeto ----------

    @GetMapping("/admin/projetos/{id}")
    public String editForm(@PathVariable String id, Model model) {
        var project = service.get(id);
        model.addAttribute("form", ProjectForm.from(project));
        model.addAttribute("projetoId", project.getId());
        model.addAttribute("slug", project.getSlug());
        return FORM;
    }

    @PostMapping("/admin/projetos/{id}")
    public String update(@PathVariable String id,
                         @Valid @ModelAttribute("form") ProjectForm form,
                         BindingResult result,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("projetoId", id);
            model.addAttribute("slug", service.get(id).getSlug());
            return FORM;
        }
        service.update(id, form);
        return VOLTAR_PARA_LISTA;
    }
}