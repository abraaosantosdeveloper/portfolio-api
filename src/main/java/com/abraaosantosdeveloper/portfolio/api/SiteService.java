package com.abraaosantosdeveloper.portfolio.api;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.abraaosantosdeveloper.portfolio.config.CacheConfig;
import com.abraaosantosdeveloper.portfolio.domain.project.ProjectRepository;
import com.abraaosantosdeveloper.portfolio.domain.settings.SiteSettings;
import com.abraaosantosdeveloper.portfolio.domain.settings.SiteSettingsRepository;
import com.abraaosantosdeveloper.portfolio.domain.stack.StackCategoryRepository;
import com.abraaosantosdeveloper.portfolio.domain.timeline.TimelinePhaseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SiteService {

    private final StackCategoryRepository stack;
    private final TimelinePhaseRepository timeline;
    private final ProjectRepository projects;
    private final SiteSettingsRepository settings;

    @Cacheable(CacheConfig.SITE)
    public SiteResponse getSite() {
        return SiteResponse.of(
            stack.findAllByOrderByOrderAsc(),
            settings.findById(SiteSettings.ID).orElse(null),
            timeline.findAllByOrderByStartDateDesc(),
            projects.findAllByPublishedTrueOrderByYearDesc());
    }
}
