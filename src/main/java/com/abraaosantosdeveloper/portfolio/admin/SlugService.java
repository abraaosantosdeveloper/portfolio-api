package com.abraaosantosdeveloper.portfolio.admin;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.abraaosantosdeveloper.portfolio.domain.project.Project;
import com.abraaosantosdeveloper.portfolio.domain.project.ProjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SlugService {

    private static final int MAX_LENGTH = 60;
    private static final String FALLBACK = "projeto";

    private final ProjectRepository projects;

    /** Gera um slug único a partir do título. Se colidir, acrescenta -2, -3… */
    public String generate(String title, String currentId) {
        String base = slugify(title);
        String candidate = base;
        int suffix = 2;
        while (isTaken(candidate, currentId)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    /** Só a transformação do texto: minúsculas, sem acento, hífens. */
    static String slugify(String title) {
        if (title == null) {
            return FALLBACK;
        }
        String semAcento = Normalizer.normalize(title, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "");
        String slug = semAcento.toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-+|-+$", "");
        if (slug.length() > MAX_LENGTH) {
            slug = slug.substring(0, MAX_LENGTH).replaceAll("-+$", "");
        }
        return slug.isEmpty() ? FALLBACK : slug;
    }

    private boolean isTaken(String slug, String currentId) {
        Optional<Project> existing = projects.findBySlug(slug);
        return existing.isPresent() && !existing.get().getId().equals(currentId);
    }
}