package fr.quentincillierre.hangman.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class KeyboardAndTimerTest {

    @Test
    void testDifficultyBasedTimerValues() {
        PlayerData player = new PlayerData();

        // Easy
        player.setDifficulty(Difficulty.EASY);
        player.resetTime();
        assertEquals(60, player.getRemainingTime());

        // Medium
        player.setDifficulty(Difficulty.MEDIUM);
        player.resetTime();
        assertEquals(30, player.getRemainingTime());

        // Hard
        player.setDifficulty(Difficulty.HARD);
        player.resetTime();
        assertEquals(20, player.getRemainingTime());
    }
}
