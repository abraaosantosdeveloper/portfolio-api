package com.abraaosantosdeveloper.portfolio.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TimelineAdminController {

    private static final String PAGE = "admin/trajetoria";
    private static final String BACK = "redirect:/admin/trajetoria";

    private final TimelineAdminService service;

    // ---------- Telas ----------

    /** Lista + cartão "Subiu de fase?" (painel fechado). */
    @GetMapping("/admin/trajetoria")
    public String page(Model model) {
        loadList(model);
        return PAGE;
    }

    @GetMapping("/admin/trajetoria/nova")
    public String newPhase(Model model) {
        loadList(model);
        model.addAttribute("faseForm", PhaseForm.empty());
        return PAGE;
    }

    @GetMapping("/admin/trajetoria/{id}")
    public String edit(@PathVariable String id, Model model) {
        var phase = service.get(id);
        loadList(model);
        boolean isCurrent = id.equals(service.settings().getCurrentPhaseId());
        model.addAttribute("faseForm", PhaseForm.from(phase, isCurrent));
        model.addAttribute("faseEditadaId", id);
        return PAGE;
    }

    // ---------- Ações ----------

    @PostMapping("/admin/trajetoria")
    public String create(@Valid @ModelAttribute("faseForm") PhaseForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            loadList(model);
            return PAGE;
        }
        var phase = service.create(form);
        redirect.addFlashAttribute("toast", toast(phase.getTitle(), form.isCurrent()));
        return BACK;
    }

    @PostMapping("/admin/trajetoria/{id}")
    public String update(@PathVariable String id,
                         @Valid @ModelAttribute("faseForm") PhaseForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            service.get(id); // 404 se não existir
            loadList(model);
            model.addAttribute("faseEditadaId", id);
            return PAGE;
        }
        var phase = service.update(id, form);
        redirect.addFlashAttribute("toast", "“" + phase.getTitle() + "” foi salva.");
        return BACK;
    }

    @PostMapping("/admin/trajetoria/{id}/excluir")
    public String delete(@PathVariable String id, RedirectAttributes redirect) {
        var phase = service.delete(id);
        redirect.addFlashAttribute("toast", "“" + phase.getTitle() + "” foi excluída.");
        return BACK;
    }

    @PostMapping("/admin/trajetoria/proxima-fase")
    public String nextPhase(@RequestParam(name = "texto", required = false) String texto,
                            RedirectAttributes redirect) {
        service.updateNextPhaseText(texto);
        redirect.addFlashAttribute("toast", "Texto da próxima fase salvo.");
        return BACK;
    }

    // ---------- Apoio ----------

    /** O que a lista (lado esquerdo) sempre precisa. */
    private void loadList(Model model) {
        var settings = service.settings();
        model.addAttribute("fases", service.list());
        model.addAttribute("faseAtualId", settings.getCurrentPhaseId());
        model.addAttribute("proximaFaseTexto", settings.getNextPhaseText());
        model.addAttribute("faseAtual", service.currentPhase());
    }

    private static String toast(String title, boolean current) {
        return current
            ? "“" + title + "” agora é a sua fase atual."
            : "“" + title + "” foi adicionada ao histórico.";
    }
}
