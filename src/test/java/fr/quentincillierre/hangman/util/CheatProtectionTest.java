package fr.quentincillierre.hangman.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.quentincillierre.hangman.model.Category;
import fr.quentincillierre.hangman.model.Difficulty;
import fr.quentincillierre.hangman.model.HangmanModel;
import fr.quentincillierre.hangman.model.PlayerData;
import fr.quentincillierre.hangman.model.Word;

class CheatProtectionTest {

    @BeforeEach
    void setUp() {
        GameSession.newGame();
    }

    @Test
    void switchingCharacterShouldResetStreakAndClearActiveRound() {
        PlayerData playerData = GameSession.getPlayerData();
        playerData.setSelectedCharacter("boy1");
        playerData.setCurrentStreak(7);

        Word word = new Word(Category.ANIMALS, Difficulty.EASY, "BEAR", "A wild mammal.");
        HangmanModel model = new HangmanModel("BEAR");
        model.tryLetter('x'); // 1 wrong guess

        GameSession.setActiveRound(model, word);
        assertTrue(GameSession.hasActiveRound());

        // Simulate character switch to "girl1"
        String previousChar = playerData.getSelectedCharacter();
        String newChar = "girl1";
        if (previousChar != null && !previousChar.equalsIgnoreCase(newChar)) {
            playerData.resetStreak();
            GameSession.clearActiveRound();
        }
        playerData.setSelectedCharacter(newChar);

        assertEquals(0, playerData.getCurrentStreak());
        assertFalse(GameSession.hasActiveRound());
    }

    @Test
    void selectingSameCharacterShouldPreserveStreakAndKeepActiveRound() {
        PlayerData playerData = GameSession.getPlayerData();
        playerData.setSelectedCharacter("boy1");
        playerData.setCurrentStreak(7);

        Word word = new Word(Category.ANIMALS, Difficulty.EASY, "BEAR", "A wild mammal.");
        HangmanModel model = new HangmanModel("BEAR");
        model.tryLetter('x'); // 1 wrong guess

        GameSession.setActiveRound(model, word);
        assertTrue(GameSession.hasActiveRound());

        // Simulate character selection of "boy1" again
        String previousChar = playerData.getSelectedCharacter();
        String newChar = "boy1";
        if (previousChar != null && !previousChar.equalsIgnoreCase(newChar)) {
            playerData.resetStreak();
            GameSession.clearActiveRound();
        }
        playerData.setSelectedCharacter(newChar);

        assertEquals(7, playerData.getCurrentStreak());
        assertTrue(GameSession.hasActiveRound());
        assertEquals("BEAR", GameSession.getActiveWord().getWord());
        assertEquals(1, GameSession.getActiveModel().getCurrentWrongs());
    }
}
