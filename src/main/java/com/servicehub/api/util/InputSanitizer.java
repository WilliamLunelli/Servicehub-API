package com.servicehub.api.util;

import java.util.regex.Pattern;

/**
 * Sanitizacao de texto livre: remove tags HTML, normaliza espacos e neutraliza caracteres
 * perigosos restantes. Evita que scripts armazenados (XSS) sejam devolvidos pela API.
 */
public final class InputSanitizer {

    private static final Pattern HTML_TAGS = Pattern.compile("<[^>]*>");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private InputSanitizer() {
    }

    public static String clean(String value) {
        if (value == null) {
            return null;
        }
        String withoutTags = HTML_TAGS.matcher(value).replaceAll("");
        String normalized = WHITESPACE.matcher(withoutTags).replaceAll(" ").trim();
        return normalized
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
