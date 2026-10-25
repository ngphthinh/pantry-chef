package org.group12.feature.recipe.enums;

import lombok.Getter;

/**
 * Độ khó. Nhà cung cấp bên ngoài không trả độ khó nên được suy ra từ thời gian chuẩn bị:
 * tới 30 phút là EASY, tới 60 phút là MEDIUM, lâu hơn là HARD.
 */
@Getter
public enum Difficulty {
    EASY(30),
    MEDIUM(60),
    HARD(null);

    /** Thời gian chuẩn bị tối đa (phút) của mức này, {@code null} = không giới hạn. Khai báo theo thứ tự tăng dần. */
    private final Integer maxMinutes;

    Difficulty(Integer maxMinutes) {
        this.maxMinutes = maxMinutes;
    }

    public static Difficulty fromReadyInMinutes(Integer minutes) {
        if (minutes == null) {
            return null;
        }
        for (Difficulty difficulty : values()) {
            if (difficulty.maxMinutes == null || minutes <= difficulty.maxMinutes) {
                return difficulty;
            }
        }
        return HARD;
    }
}
