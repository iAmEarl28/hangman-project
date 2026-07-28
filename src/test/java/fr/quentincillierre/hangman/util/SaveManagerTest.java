package fr.quentincillierre.hangman.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.quentincillierre.hangman.model.Category;
import fr.quentincillierre.hangman.model.Difficulty;
import fr.quentincillierre.hangman.model.HangmanModel;
import fr.quentincillierre.hangman.model.PlayerData;
import fr.quentincillierre.hangman.model.Word;

class SaveManagerTest {

    @BeforeEach
    @AfterEach
    void cleanUp() {
        SaveManager.deleteSaveFile();
    }

    @Test
    void testSaveAndLoadGame() {
        assertFalse(SaveManager.hasSaveFile());

        PlayerData playerData = new PlayerData();
        playerData.setDifficulty(Difficulty.HARD);
        playerData.setCategory(Category.ANIMALS);
        playerData.setSelectedCharacter("robot");
        playerData.setDiamonds(42);
        playerData.setCurrentStreak(5);
        playerData.setHighestStreak(12);
        playerData.setRemainingAttempts(4);
        playerData.setRemainingTime(35);
        playerData.setHintUsed(true);

        Word word = new Word(Category.ANIMALS, Difficulty.HARD, "ELEPHANT", "A large mammal.");
        playerData.setCurrentWord(word);

        HangmanModel model = new HangmanModel(word.getWord());
        model.tryLetter('e');
        model.tryLetter('z');

        boolean saved = SaveManager.saveGame(playerData, model, word);
        assertTrue(saved);
        assertTrue(SaveManager.hasSaveFile());

        SaveManager.SavedGameState loadedState = SaveManager.loadGame();
        assertNotNull(loadedState);
        assertEquals(Difficulty.HARD, loadedState.playerData.getDifficulty());
        assertEquals(Category.ANIMALS, loadedState.playerData.getCategory());
        assertEquals("robot", loadedState.playerData.getSelectedCharacter());
        assertEquals(42, loadedState.playerData.getDiamonds());
        assertEquals(5, loadedState.playerData.getCurrentStreak());
        assertEquals(12, loadedState.playerData.getHighestStreak());
        assertEquals(4, loadedState.playerData.getRemainingAttempts());
        assertEquals(35, loadedState.playerData.getRemainingTime());
        assertTrue(loadedState.playerData.isHintUsed());

        assertEquals("ELEPHANT", loadedState.word.getWord());
        assertEquals("A large mammal.", loadedState.word.getDefinition());

        assertEquals("ELEPHANT", loadedState.model.getWordToGuess());
        assertEquals(1, loadedState.model.getCurrentWrongs());
        assertTrue(loadedState.model.getGuessedLetter().contains('e'));
        assertTrue(loadedState.model.getGuessedLetter().contains('z'));
    }

    @Test
    void testDeleteSaveFile() {
        PlayerData playerData = new PlayerData();
        Word word = new Word(Category.ANIMALS, Difficulty.EASY, "DOG", "Friendly pet.");
        HangmanModel model = new HangmanModel("DOG");

        SaveManager.saveGame(playerData, model, word);
        assertTrue(SaveManager.hasSaveFile());

        SaveManager.deleteSaveFile();
        assertFalse(SaveManager.hasSaveFile());
    }
}
