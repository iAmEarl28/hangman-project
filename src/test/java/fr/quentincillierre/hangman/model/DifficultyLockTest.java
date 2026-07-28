package fr.quentincillierre.hangman.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DifficultyLockTest {

    @Test
    void testDifficultyUnlockingRequirements() {
        PlayerData player = new PlayerData();

        // Initially 0 streak -> Medium and Hard should be locked
        assertTrue(player.getHighestStreak() < 10);
        assertTrue(player.getHighestStreak() < 20);

        // Increase streak to 10
        for (int i = 0; i < 10; i++) {
            player.increaseStreak();
        }

        assertEquals(10, player.getHighestStreak());
        assertFalse(player.getHighestStreak() < 10); // Medium is unlocked!
        assertTrue(player.getHighestStreak() < 20);  // Hard still locked

        // Increase streak to 20
        for (int i = 0; i < 10; i++) {
            player.increaseStreak();
        }

        assertEquals(20, player.getHighestStreak());
        assertFalse(player.getHighestStreak() < 20); // Hard is unlocked!
    }

    private void assertEquals(int expected, int actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
}
