package com.abraaosantosdeveloper.portfolio.admin;

import java.util.List;
import java.util.Objects;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.abraaosantosdeveloper.portfolio.config.CacheConfig;
import com.abraaosantosdeveloper.portfolio.domain.settings.SiteSettings;
import com.abraaosantosdeveloper.portfolio.domain.settings.SiteSettingsRepository;
import com.abraaosantosdeveloper.portfolio.domain.timeline.TimelinePhase;
import com.abraaosantosdeveloper.portfolio.domain.timeline.TimelinePhaseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimelineAdminService {

    private final TimelinePhaseRepository phases;
    private final SiteSettingsRepository settingsRepository;

    public List<TimelinePhase> list() {
        return phases.findAllByOrderByStartDateDesc();
    }

    public TimelinePhase get(String id) {
        return phases.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fase não encontrada"));
    }

    /** O documento único "site". Se ainda não existir (banco novo), devolve um em memória. */
    public SiteSettings settings() {
        return settingsRepository.findById(SiteSettings.ID)
            .orElseGet(() -> SiteSettings.builder().id(SiteSettings.ID).build());
    }

    /** A fase marcada como atual, ou null. */
    public TimelinePhase currentPhase() {
        String id = settings().getCurrentPhaseId();
        return id == null ? null : phases.findById(id).orElse(null);
    }

    @CacheEvict(value = CacheConfig.SITE, allEntries = true)
    public TimelinePhase create(PhaseForm form) {
        var phase = new TimelinePhase();
        form.applyTo(phase);
        phase = phases.save(phase);
        applyCurrent(phase, form);
        return phase;
    }

    @CacheEvict(value = CacheConfig.SITE, allEntries = true)
    public TimelinePhase update(String id, PhaseForm form) {
        var phase = get(id);
        form.applyTo(phase);
        phase = phases.save(phase);
        applyCurrent(phase, form);
        return phase;
    }

    @CacheEvict(value = CacheConfig.SITE, allEntries = true)
    public TimelinePhase delete(String id) {
        var phase = get(id);
        var settings = settings();
        if (Objects.equals(settings.getCurrentPhaseId(), id)) {
            settings.setCurrentPhaseId(null);
            settingsRepository.save(settings);
        }
        phases.delete(phase);
        return phase;
    }

    @CacheEvict(value = CacheConfig.SITE, allEntries = true)
    public void updateNextPhaseText(String text) {
        var settings = settings();
        String clean = text == null ? "" : text.trim();
        settings.setNextPhaseText(clean.length() > 120 ? clean.substring(0, 120) : clean);
        settingsRepository.save(settings);
    }

    /**
     * Regra da fase atual:
     * - marcada: ela vira a atual; a anterior (se havia outra) recebe o fim informado em "Quando ela terminou?";
     * - desmarcada numa fase que era a atual: o site fica sem destaque.
     * Primeiro grava o destaque (um update só); o fim da anterior vem depois, então o destaque nunca fica errado.
     */
    private void applyCurrent(TimelinePhase phase, PhaseForm form) {
        var settings = settings();
        String previousId = settings.getCurrentPhaseId();

        if (form.isCurrent()) {
            if (!phase.getId().equals(previousId)) {
                settings.setCurrentPhaseId(phase.getId());
                settingsRepository.save(settings);

                var previousEnd = form.previousEndDate();
                if (previousId != null && previousEnd != null) {
                    phases.findById(previousId).ifPresent(previous -> {
                        previous.setEndDate(previousEnd);
                        phases.save(previous);
                    });
                }
            }
        } else if (phase.getId().equals(previousId)) {
            settings.setCurrentPhaseId(null);
            settingsRepository.save(settings);
        }
    }
}
