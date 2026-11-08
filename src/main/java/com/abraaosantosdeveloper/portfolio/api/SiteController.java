package com.abraaosantosdeveloper.portfolio.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService site;

    @GetMapping("/site")
    public SiteResponse getSite() {
        return site.getSite();
    }
}