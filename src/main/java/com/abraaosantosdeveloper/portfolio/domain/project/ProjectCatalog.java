package com.abraaosantosdeveloper.portfolio.domain.project;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ProjectCatalog {

    private ProjectCatalog() {}

    public record Icon(String id, String label, String path) {}

    public record Color(String id, String family, String label, String face, String shadow) {}

    public static final String DEFAULT_ICON = "code";
    public static final String DEFAULT_COLOR = "menta-2";

    public static final List<Icon> ICONS = List.of(
        new Icon("headset", "Atendimento",
            "M4 14v-2a8 8 0 0 1 16 0v2 M4 13h1a1.5 1.5 0 0 1 1.5 1.5v3A1.5 1.5 0 0 1 5 19H4a1.5 1.5 0 0 1-1.5-1.5v-3A1.5 1.5 0 0 1 4 13z M19 13h1a1.5 1.5 0 0 1 1.5 1.5v3A1.5 1.5 0 0 1 20 19h-1a1.5 1.5 0 0 1-1.5-1.5v-3A1.5 1.5 0 0 1 19 13z M19.5 19v.5a2.5 2.5 0 0 1-2.5 2.5h-3"),
        new Icon("robot", "IA / robô",
            "M6 9h8a3 3 0 0 1 3 3v5a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3v-5a3 3 0 0 1 3-3z M10 9V6 M10 3a1.5 1.5 0 1 1 0 3a1.5 1.5 0 1 1 0-3z M7.5 14h.01 M12.5 14h.01 M8 17h4 M20 3v4 M18 5h4"),
        new Icon("file", "Automação de documentos",
            "M13 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h4 M13 3v5h5 M17 14.5a2.5 2.5 0 1 1 0 5a2.5 2.5 0 1 1 0-5z M17 12.5V14 M17 20v1.5 M12.5 17H14 M20 17h1.5"),
        new Icon("chat", "Chat",
            "M3 4h12v8H8l-4 3v-3H3z M18 8h3v8h-1v3l-3-3h-6v-2"),
        new Icon("database", "Dados",
            "M4 5a8 3 0 1 0 16 0a8 3 0 1 0-16 0 M4 5v14c0 1.7 3.6 3 8 3s8-1.3 8-3V5 M4 12c0 1.7 3.6 3 8 3s8-1.3 8-3"),
        new Icon("code", "API / código",
            "M8 6l-6 6 6 6 M16 6l6 6-6 6 M14 4l-4 16"),
        new Icon("terminal", "CLI",
            "M4 17l6-6-6-6 M12 19h8"),
        new Icon("globe", "Site / web",
            "M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18z M3 12h18 M12 3c2.5 2.5 3.5 5.5 3.5 9s-1 6.5-3.5 9c-2.5-2.5-3.5-5.5-3.5-9s1-6.5 3.5-9z"),
        new Icon("cart", "E-commerce",
            "M3 4h2l2.5 11h10L20 7H6.5 M9 20a1 1 0 1 0 0-2a1 1 0 1 0 0 2z M17 20a1 1 0 1 0 0-2a1 1 0 1 0 0 2z"),
        new Icon("chart", "Relatórios",
            "M4 20V10 M10 20V4 M16 20v-7 M22 20H2"),
        new Icon("shield", "Segurança",
            "M12 3 4 6v6c0 5 3.5 8 8 9 4.5-1 8-4 8-9V6z M9 12l2 2 4-4"),
        new Icon("calendar", "Agenda",
            "M5 5h14a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z M3 10h18 M8 3v4 M16 3v4")
    );

    public static final List<Color> COLORS = List.of(
        new Color("menta-1", "Menta", "Menta claro", "#34C79A", "#22966F"),
        new Color("menta-2", "Menta", "Menta médio", "#12A37A", "#0B7556"),
        new Color("menta-3", "Menta", "Menta escuro", "#0F7B5F", "#0A5442"),
        new Color("teal-1", "Petróleo", "Petróleo claro", "#2BB5B8", "#1C8588"),
        new Color("teal-2", "Petróleo", "Petróleo médio", "#14929A", "#0D6A70"),
        new Color("teal-3", "Petróleo", "Petróleo escuro", "#0E6E76", "#094C52"),
        new Color("azul-1", "Azul", "Azul claro", "#5B9BF8", "#3A73CC"),
        new Color("azul-2", "Azul", "Azul médio", "#3B82F6", "#2563C9"),
        new Color("azul-3", "Azul", "Azul escuro", "#2459C2", "#183E8A"),
        new Color("lilas-1", "Lilás", "Lilás claro", "#A48BF5", "#7C63D1"),
        new Color("lilas-2", "Lilás", "Lilás médio", "#8B6CF0", "#6447C9"),
        new Color("lilas-3", "Lilás", "Lilás escuro", "#6A4FD6", "#4A349E"),
        new Color("coral-1", "Coral", "Coral claro", "#FF8A7A", "#D4604F"),
        new Color("coral-2", "Coral", "Coral médio", "#F06A5A", "#C24A3C"),
        new Color("coral-3", "Coral", "Coral escuro", "#D9503F", "#A3372A"),
        new Color("ambar-1", "Âmbar", "Âmbar claro", "#FFC14D", "#D69620"),
        new Color("ambar-2", "Âmbar", "Âmbar médio", "#F5A524", "#C47F0E"),
        new Color("ambar-3", "Âmbar", "Âmbar escuro", "#DB8A0F", "#A6670A")
    );

    private static final Map<String, Icon> ICONS_BY_ID =
        ICONS.stream().collect(Collectors.toMap(Icon::id, Function.identity()));

    private static final Map<String, Color> COLORS_BY_ID =
        COLORS.stream().collect(Collectors.toMap(Color::id, Function.identity()));

    /** Cores agrupadas por família, na ordem acima. Usado para montar a grade no formulário. */
    public static Map<String, List<Color>> colorsByFamily() {
        return COLORS.stream().collect(Collectors.groupingBy(
            Color::family, LinkedHashMap::new, Collectors.toList()));
    }

    public static boolean isValidIcon(String id) {
        return id != null && ICONS_BY_ID.containsKey(id);
    }

    public static boolean isValidColor(String id) {
        return id != null && COLORS_BY_ID.containsKey(id);
    }

    public static Icon icon(String id) {
        return ICONS_BY_ID.getOrDefault(id, ICONS_BY_ID.get(DEFAULT_ICON));
    }

    public static Color color(String id) {
        return COLORS_BY_ID.getOrDefault(id, COLORS_BY_ID.get(DEFAULT_COLOR));
    }
}