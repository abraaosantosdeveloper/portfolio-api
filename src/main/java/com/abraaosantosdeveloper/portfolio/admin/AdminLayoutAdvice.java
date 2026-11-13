package com.abraaosantosdeveloper.portfolio.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.abraaosantosdeveloper.portfolio.domain.project.ProjectRepository;

import lombok.RequiredArgsConstructor;

/**
 * Dados que a barra lateral do painel usa em todas as telas:
 * o contador de projetos e o link "Ver site".
 */
@ControllerAdvice(basePackages = "com.abraaosantosdeveloper.portfolio.admin")
@RequiredArgsConstructor
public class AdminLayoutAdvice {

    private final ProjectRepository projects;

    @Value("${app.site-url:https://abraaosantosdeveloper.github.io}")
    private String siteUrl;

    @ModelAttribute("totalProjetos")
    public long totalProjetos() {
        return projects.count();
    }

    @ModelAttribute("siteUrl")
    public String siteUrl() {
        return siteUrl;
    }
}
