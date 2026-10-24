package org.group12.common.utils;

import java.text.Normalizer;
import java.util.Locale;

public final class TextUtil {

    /**
     * Cặp chuỗi dùng cho hàm translate() của PostgreSQL để bỏ dấu tiếng Việt ngay trong query:
     * translate(name, ACCENT_FROM, ACCENT_TO). Ký tự thứ i của ACCENT_FROM được đổi thành ký tự thứ i của ACCENT_TO.
     */
    public static final String ACCENT_FROM;
    public static final String ACCENT_TO;

    static {
        // ký tự đầu mỗi nhóm là chữ gốc không dấu, các ký tự sau là biến thể có dấu
        String[] groups = {
                "aàáạảãâầấậẩẫăằắặẳẵ",
                "eèéẹẻẽêềếệểễ",
                "iìíịỉĩ",
                "oòóọỏõôồốộổỗơờớợởỡ",
                "uùúụủũưừứựửữ",
                "yỳýỵỷỹ",
                "dđ"
        };
        StringBuilder from = new StringBuilder();
        StringBuilder to = new StringBuilder();
        for (String group : groups) {
            char base = group.charAt(0);
            for (int i = 1; i < group.length(); i++) {
                char c = group.charAt(i);
                from.append(c).append(Character.toUpperCase(c));
                to.append(base).append(base);
            }
        }
        ACCENT_FROM = from.toString();
        ACCENT_TO = to.toString();
    }

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