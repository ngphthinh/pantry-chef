package org.group12.common.utils;

import java.text.Normalizer;
import java.util.Locale;

public final class TextUtil {

    private TextUtil() {
    }
    public static String normalizeForSearch(String input) {
        if (input == null) {
            return null;
        }
        String decomposed = Normalizer.normalize(input, Normalizer.Form.NFD);
        String noAccent = decomposed.replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');
        return noAccent.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }
}