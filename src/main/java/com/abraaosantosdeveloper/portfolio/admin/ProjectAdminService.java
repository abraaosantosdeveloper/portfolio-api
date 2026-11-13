package com.abraaosantosdeveloper.portfolio.admin;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.abraaosantosdeveloper.portfolio.config.CacheConfig;
import com.abraaosantosdeveloper.portfolio.domain.project.Project;
import com.abraaosantosdeveloper.portfolio.domain.project.ProjectRepository;
import com.abraaosantosdeveloper.portfolio.domain.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectAdminService {

    private final ProjectRepository projects;
    private final UserRepository users;
    private final SlugService slugs;

    public List<Project> list() {
        return projects.findAll(Sort.by(Sort.Order.desc("year"), Sort.Order.asc("title")));
    }

    public Project get(String id) {
        return projects.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado"));
    }

    @CacheEvict(value = CacheConfig.SITE, allEntries = true)
    public Project create(ProjectForm form, String authorUsername) {
        var author = users.findByUsername(authorUsername)
            .orElseThrow(() -> new IllegalStateException("Usuário da sessão não existe mais"));

        var project = new Project();
        form.applyTo(project);
        project.setSlug(slugs.generate(form.getTitle(), null));
        project.setAuthorId(author.getId());
        return projects.save(project);
    }

    @CacheEvict(value = CacheConfig.SITE, allEntries = true)
    public void delete(String id) {
        projects.delete(get(id)); // get() devolve 404 se não existir
    }

    @CacheEvict(value = CacheConfig.SITE, allEntries = true)
    public Project update(String id, ProjectForm form) {
        var project = get(id);
        form.applyTo(project); // slug, imagens e autor continuam como estavam
        return projects.save(project);
    }
}