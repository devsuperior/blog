package br.com.devsuperior.dev_xp_ai.util;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TextNormalizer {

    private TextNormalizer() {
    }

    public static String toTitleCase(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String normalized = text.trim().replaceAll("\\s+", " ");
        return Arrays.stream(normalized.split(" "))
                .map(TextNormalizer::capitalizeWord)
                .collect(Collectors.joining(" "));
    }

    public static String toLowerCaseTrimmed(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().toLowerCase(Locale.ROOT);
    }

    public static String toUpperCaseTrimmed(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().toUpperCase(Locale.ROOT);
    }

    public static String serializeSkills(List<String> skills) {
        if (skills == null) {
            return "";
        }
        return skills.stream()
                .map(String::trim)
                .collect(Collectors.joining(","));
    }

    public static List<String> deserializeSkills(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private static String capitalizeWord(String word) {
        String lower = word.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}

