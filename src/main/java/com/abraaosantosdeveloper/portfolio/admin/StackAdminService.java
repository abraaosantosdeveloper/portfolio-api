package com.abraaosantosdeveloper.portfolio.admin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.abraaosantosdeveloper.portfolio.config.CacheConfig;
import com.abraaosantosdeveloper.portfolio.domain.stack.StackCategory;
import com.abraaosantosdeveloper.portfolio.domain.stack.StackCategoryRepository;
import com.abraaosantosdeveloper.portfolio.domain.stack.StackItem;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StackAdminService {

    static final int MAX_TITLE = 40;
    static final int MAX_ITEM = 30;

    private final StackCategoryRepository categories;

    public List<StackCategory> list() {
        return categories.findAllByOrderByOrderAsc();
    }

    /**
     * Salva a tela inteira:
     * - a posição na lista vira o "order";
     * - itens que já existiam mantêm description e since (casados pelo nome);
     * - categoria existente com nome vazio é apagada;
     * - categoria nova sem nome e sem itens é ignorada; sem nome e com itens é recusada.
     */
    @CacheEvict(value = CacheConfig.SITE, allEntries = true)
    public void save(StackForm form) {
        Map<String, StackCategory> existing = categories.findAll().stream()
            .collect(Collectors.toMap(StackCategory::getId, Function.identity()));

        List<StackCategory> toSave = new ArrayList<>();
        List<StackCategory> toDelete = new ArrayList<>();
        int order = 1;

        for (StackForm.Categoria input : form.getCategorias()) {
            if (input == null) {
                continue;
            }
            String title = clean(input.getTitle(), MAX_TITLE);
            List<String> names = cleanItems(input.getItems());
            boolean isNew = input.getId() == null || input.getId().isBlank();

            if (isNew) {
                if (title.isEmpty()) {
                    if (!names.isEmpty()) {
                        throw new IllegalArgumentException("Dê um nome à categoria nova.");
                    }
                    continue;
                }
                toSave.add(StackCategory.builder()
                    .title(title)
                    .order(order++)
                    .items(names.stream().map(n -> new StackItem(n, null, null)).toList())
                    .build());
                continue;
            }

            StackCategory category = existing.get(input.getId());
            if (category == null) {
                continue; // apagada em outra aba: ignora
            }
            if (title.isEmpty()) {
                toDelete.add(category);
                continue;
            }
            category.setTitle(title);
            category.setOrder(order++);
            category.setItems(merge(category.getItems(), names));
            toSave.add(category);
        }

        categories.deleteAll(toDelete);
        categories.saveAll(toSave);
    }

    /** Mantém description e since dos itens que já existiam. */
    private static List<StackItem> merge(List<StackItem> current, List<String> names) {
        Map<String, StackItem> byName = new LinkedHashMap<>();
        if (current != null) {
            current.forEach(item -> byName.putIfAbsent(key(item.name()), item));
        }
        return names.stream()
            .map(n -> {
                StackItem old = byName.get(key(n));
                return old == null ? new StackItem(n, null, null) : new StackItem(n, old.description(), old.since());
            })
            .toList();
    }

    private static List<String> cleanItems(List<String> raw) {
        Map<String, String> unique = new LinkedHashMap<>();
        if (raw != null) {
            for (String item : raw) {
                String name = clean(item, MAX_ITEM);
                if (!name.isEmpty()) {
                    unique.putIfAbsent(key(name), name);
                }
            }
        }
        return new ArrayList<>(unique.values());
    }

    private static String clean(String text, int max) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        return trimmed.length() > max ? trimmed.substring(0, max) : trimmed;
    }

    private static String key(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }
}
