package com.abraaosantosdeveloper.portfolio.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class StackAdminController {

    private final StackAdminService service;

    @GetMapping("/admin/stack")
    public String page(Model model) {
        model.addAttribute("categorias", service.list());
        return "admin/stack";
    }

    @PostMapping("/admin/stack")
    public String save(@ModelAttribute StackForm form, RedirectAttributes redirect) {
        try {
            service.save(form);
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/admin/stack";
        }
        return "redirect:/admin/stack?salvo";
    }
}
