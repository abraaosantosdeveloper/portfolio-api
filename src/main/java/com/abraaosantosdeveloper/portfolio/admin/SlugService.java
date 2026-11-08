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
        String slug = toSlugChars(semAcento.toLowerCase(Locale.ROOT));
        if (slug.length() > MAX_LENGTH) {
            slug = trimDashes(slug.substring(0, MAX_LENGTH));
        }
        return slug.isEmpty() ? FALLBACK : slug;
    }

    private static String toSlugChars(String text) {
        var slug = new StringBuilder(text.length());
        boolean lastWasDash = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                slug.append(c);
                lastWasDash = false;
            } else if (!lastWasDash) {
                slug.append('-');
                lastWasDash = true;
            }
        }
        return trimDashes(slug.toString());
    }

    private static String trimDashes(String text) {
        int start = 0;
        int end = text.length();
        while (start < end && text.charAt(start) == '-') {
            start++;
        }
        while (end > start && text.charAt(end - 1) == '-') {
            end--;
        }
        return text.substring(start, end);
    }

    private boolean isTaken(String slug, String currentId) {
        Optional<Project> existing = projects.findBySlug(slug);
        return existing.isPresent() && !existing.get().getId().equals(currentId);
    }
}