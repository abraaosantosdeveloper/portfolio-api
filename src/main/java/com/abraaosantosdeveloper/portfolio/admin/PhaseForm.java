package com.abraaosantosdeveloper.portfolio.admin;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import com.abraaosantosdeveloper.portfolio.domain.timeline.PhaseKind;
import com.abraaosantosdeveloper.portfolio.domain.timeline.TimelinePhase;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Formulário "Nova fase" / "Editar fase". Datas no formato mm/aaaa. */
@Getter
@Setter
@NoArgsConstructor
public class PhaseForm {

    private static final String MES_ANO = "^$|^(0[1-9]|1[0-2])/\\d{4}$";
    private static final DateTimeFormatter MM_YYYY = DateTimeFormatter.ofPattern("MM/yyyy");
    private static final int MAX_TECHNOLOGIES = 12;

    @NotNull(message = "Escolha o tipo")
    private PhaseKind kind = PhaseKind.WORK;

    @NotBlank(message = "Dê um título para a fase.")
    @Size(max = 100, message = "No máximo 100 caracteres")
    private String title;

    @Size(max = 100, message = "No máximo 100 caracteres")
    private String institution;

    @Pattern(regexp = MES_ANO, message = "Use mm/aaaa")
    private String start;

    @Pattern(regexp = MES_ANO, message = "Use mm/aaaa")
    private String end; // chega null quando "Ainda estou nessa fase" está marcado (campo desabilitado)

    /** "Ainda estou nessa fase" */
    private boolean ongoing = true;

    /** "Esta é a minha fase atual" */
    private boolean current = true;

    /** Fim da fase que deixa de ser a atual (opcional). */
    @Pattern(regexp = MES_ANO, message = "Use mm/aaaa")
    private String previousEnd;

    @Size(max = 160, message = "No máximo 160 caracteres")
    private String description;

    /** "O que eu faço / fiz": uma ação por linha. */
    @Size(max = 2000, message = "No máximo 2000 caracteres")
    private String highlights;

    /** Tecnologias separadas por vírgula. */
    @Size(max = 400, message = "Texto muito longo")
    private String technologies;

    @AssertTrue(message = "O fim não pode ser antes do início")
    public boolean isPeriodValid() {
        LocalDate s = parse(start);
        LocalDate e = ongoing ? null : parse(end);
        return s == null || e == null || !e.isBefore(s);
    }

    @AssertTrue(message = "Use até 12 tecnologias")
    public boolean isTechnologiesValid() {
        return technologyList().size() <= MAX_TECHNOLOGIES;
    }

    public static PhaseForm empty() {
        return new PhaseForm();
    }

    public static PhaseForm from(TimelinePhase phase, boolean isCurrent) {
        var form = new PhaseForm();
        form.setKind(phase.getKind() != null ? phase.getKind() : PhaseKind.WORK);
        form.setTitle(phase.getTitle());
        form.setInstitution(phase.getInstitution());
        form.setStart(format(phase.getStartDate()));
        form.setEnd(format(phase.getEndDate()));
        form.setOngoing(phase.getEndDate() == null);
        form.setCurrent(isCurrent);
        form.setDescription(phase.getDescription());
        form.setHighlights(phase.getHighlights() == null ? "" : String.join("\n", phase.getHighlights()));
        form.setTechnologies(phase.getTechnologies() == null ? "" : String.join(", ", phase.getTechnologies()));
        return form;
    }

    public void applyTo(TimelinePhase phase) {
        phase.setKind(kind);
        phase.setTitle(title.trim());
        phase.setInstitution(blankToNull(institution));
        phase.setStartDate(parse(start));
        phase.setEndDate(ongoing ? null : parse(end));
        phase.setDescription(blankToNull(description));
        phase.setHighlights(highlightList());
        phase.setTechnologies(technologyList());
    }

    public LocalDate previousEndDate() {
        return parse(previousEnd);
    }

    List<String> highlightList() {
        if (highlights == null || highlights.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(highlights.split("\\R"))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    }

    List<String> technologyList() {
        if (technologies == null || technologies.isBlank()) {
            return new ArrayList<>();
        }
        var unique = new LinkedHashSet<String>();
        for (String part : technologies.split(",")) {
            String t = part.trim();
            if (!t.isEmpty()) {
                unique.add(t.length() > 30 ? t.substring(0, 30) : t);
            }
        }
        return new ArrayList<>(unique);
    }

    /** "03/2024" → 2024-03-01. Vazio ou inválido → null (o @Pattern já avisa). */
    static LocalDate parse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return YearMonth.parse(text.trim(), MM_YYYY).atDay(1);
        } catch (RuntimeException e) {
            return null;
        }
    }

    static String format(LocalDate date) {
        return date == null ? "" : date.format(MM_YYYY);
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }
}
