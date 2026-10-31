package org.group12.feature.recipe;

import org.group12.feature.recipe.enums.Difficulty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DifficultyTest {

    @Test
    void fromReadyInMinutes_usesThresholds() {
        assertNull(Difficulty.fromReadyInMinutes(null));
        assertEquals(Difficulty.EASY, Difficulty.fromReadyInMinutes(10));
        assertEquals(Difficulty.EASY, Difficulty.fromReadyInMinutes(30));
        assertEquals(Difficulty.MEDIUM, Difficulty.fromReadyInMinutes(31));
        assertEquals(Difficulty.MEDIUM, Difficulty.fromReadyInMinutes(60));
        assertEquals(Difficulty.HARD, Difficulty.fromReadyInMinutes(61));
    }
}
