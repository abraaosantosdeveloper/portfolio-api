package com.abraaosantosdeveloper.portfolio.api;

import java.time.LocalDate;
import java.util.List;

import com.abraaosantosdeveloper.portfolio.domain.project.Project;
import com.abraaosantosdeveloper.portfolio.domain.project.ProjectImage;
import com.abraaosantosdeveloper.portfolio.domain.project.ProjectStatus;
import com.abraaosantosdeveloper.portfolio.domain.settings.SiteSettings;
import com.abraaosantosdeveloper.portfolio.domain.stack.StackCategory;
import com.abraaosantosdeveloper.portfolio.domain.stack.StackItem;
import com.abraaosantosdeveloper.portfolio.domain.timeline.PhaseKind;
import com.abraaosantosdeveloper.portfolio.domain.timeline.TimelinePhase;

public record SiteResponse(
        List<StackCategoryDto> stack,
        TimelineDto timeline,
        List<ProjectDto> projects) {

    public static SiteResponse of(
            List<StackCategory> categories,
            SiteSettings settings,
            List<TimelinePhase> phases,
            List<Project> projects) {
        return new SiteResponse(
            categories.stream().map(StackCategoryDto::from).toList(),
            TimelineDto.of(settings, phases),
            projects.stream().map(ProjectDto::from).toList());
    }

    public record StackCategoryDto(String title, List<StackItemDto> items) {
        static StackCategoryDto from(StackCategory c) {
            return new StackCategoryDto(c.getTitle(),
                orEmpty(c.getItems()).stream().map(StackItemDto::from).toList());
        }
    }

    public record StackItemDto(String name, String description, LocalDate since) {
        static StackItemDto from(StackItem i) {
            return new StackItemDto(i.name(), i.description(), i.since());
        }
    }

    public record TimelineDto(String nextPhaseText, String currentPhaseId, List<PhaseDto> phases) {
        static TimelineDto of(SiteSettings s, List<TimelinePhase> phases) {
            return new TimelineDto(
                s == null ? null : s.getNextPhaseText(),
                s == null ? null : s.getCurrentPhaseId(),
                phases.stream().map(PhaseDto::from).toList());
        }
    }

    public record PhaseDto(
            String id,
            PhaseKind kind,
            String title,
            String institution,
            String description,
            List<String> highlights,
            List<String> technologies,
            LocalDate startDate,
            LocalDate endDate) {
        static PhaseDto from(TimelinePhase p) {
            return new PhaseDto(p.getId(), p.getKind(), p.getTitle(), p.getInstitution(),
                p.getDescription(), orEmpty(p.getHighlights()), orEmpty(p.getTechnologies()),
                p.getStartDate(), p.getEndDate());
        }
    }

    public record ProjectDto(
            String slug,
            String title,
            String description,
            String problem,
            String solution,
            Integer year,
            ProjectStatus status,
            String icon,
            String color,
            List<String> tags,
            List<ImageDto> images,
            String repoUrl,
            String liveUrl) {
        static ProjectDto from(Project p) {
            return new ProjectDto(p.getSlug(), p.getTitle(), p.getDescription(), p.getProblem(),
                p.getSolution(), p.getYear(), p.getStatus(), p.getIcon(), p.getColor(),
                orEmpty(p.getTags()),
                orEmpty(p.getImages()).stream().map(ImageDto::from).toList(),
                p.getRepoUrl(), p.getLiveUrl());
        }
    }

    public record ImageDto(String url, Integer width, Integer height) {
        static ImageDto from(ProjectImage i) {
            return new ImageDto(i.url(), i.width(), i.height());
        }
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }
}