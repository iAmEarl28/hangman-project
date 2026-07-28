package fr.quentincillierre.hangman.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import fr.quentincillierre.hangman.model.Category;
import fr.quentincillierre.hangman.model.Difficulty;
import fr.quentincillierre.hangman.model.HangmanModel;
import fr.quentincillierre.hangman.model.PlayerData;
import fr.quentincillierre.hangman.model.Word;

public class SaveManager {

    private static final String SAVE_FILE_PATH = "hangman_savegame.properties";

    public static boolean hasSaveFile() {
        File file = new File(SAVE_FILE_PATH);
        return file.exists() && file.isFile();
    }

    public static void deleteSaveFile() {
        File file = new File(SAVE_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }

    public static boolean saveGame(PlayerData playerData, HangmanModel model, Word currentWord) {
        if (playerData == null || model == null || currentWord == null) {
            return false;
        }

        Properties props = new Properties();

        // PlayerData
        props.setProperty("difficulty", playerData.getDifficulty() != null ? playerData.getDifficulty().name() : Difficulty.EASY.name());
        props.setProperty("category", playerData.getCategory() != null ? playerData.getCategory().name() : Category.ANIMALS.name());
        props.setProperty("selectedCharacter", playerData.getSelectedCharacter() != null ? playerData.getSelectedCharacter() : "boy1");
        props.setProperty("diamonds", String.valueOf(playerData.getDiamonds()));
        props.setProperty("currentStreak", String.valueOf(playerData.getCurrentStreak()));
        props.setProperty("highestStreak", String.valueOf(playerData.getHighestStreak()));
        props.setProperty("remainingAttempts", String.valueOf(playerData.getRemainingAttempts()));
        props.setProperty("remainingTime", String.valueOf(playerData.getRemainingTime()));
        props.setProperty("hintUsed", String.valueOf(playerData.isHintUsed()));
        props.setProperty("started", String.valueOf(playerData.hasStarted()));

        // Unlocked characters (comma-separated keys)
        String unlockedChars = String.join(",", playerData.getUnlockedCharacters());
        props.setProperty("unlockedCharacters", unlockedChars);

        // Admin mode
        props.setProperty("adminMode", String.valueOf(playerData.isAdminMode()));

        // Current Word
        props.setProperty("word", currentWord.getWord());
        props.setProperty("definition", currentWord.getDefinition() != null ? currentWord.getDefinition() : "");

        // HangmanModel
        props.setProperty("currentWrongs", String.valueOf(model.getCurrentWrongs()));
        String guessed = model.getGuessedLetter().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        props.setProperty("guessedLetters", guessed);

        try (FileOutputStream out = new FileOutputStream(SAVE_FILE_PATH)) {
            props.store(out, "Hangman Cutie Save Data");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static SavedGameState loadGame() {
        if (!hasSaveFile()) {
            return null;
        }

        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(SAVE_FILE_PATH)) {
            props.load(in);

            PlayerData playerData = new PlayerData();

            String diffStr = props.getProperty("difficulty", "EASY");
            try {
                playerData.setDifficulty(Difficulty.valueOf(diffStr));
            } catch (Exception e) {
                playerData.setDifficulty(Difficulty.EASY);
            }

            String catStr = props.getProperty("category", "ANIMALS");
            try {
                playerData.setCategory(Category.valueOf(catStr));
            } catch (Exception e) {
                playerData.setCategory(Category.ANIMALS);
            }

            playerData.setSelectedCharacter(props.getProperty("selectedCharacter", "boy1"));
            playerData.setDiamonds(Integer.parseInt(props.getProperty("diamonds", "0")));
            playerData.setCurrentStreak(Integer.parseInt(props.getProperty("currentStreak", "0")));
            playerData.setHighestStreak(Integer.parseInt(props.getProperty("highestStreak", "0")));
            playerData.setStarted(Boolean.parseBoolean(props.getProperty("started", "true")));

            // Unlocked characters
            String unlockedStr = props.getProperty("unlockedCharacters", "boy1");
            Set<String> unlockedSet = new HashSet<>(Arrays.asList(unlockedStr.split(",")));
            playerData.setUnlockedCharacters(unlockedSet);

            // Admin mode
            playerData.setAdminMode(Boolean.parseBoolean(props.getProperty("adminMode", "false")));

            int remainingAttempts = Integer.parseInt(props.getProperty("remainingAttempts", "10"));
            int remainingTime = Integer.parseInt(props.getProperty("remainingTime", "60"));
            boolean hintUsed = Boolean.parseBoolean(props.getProperty("hintUsed", "false"));

            playerData.setRemainingAttempts(remainingAttempts);
            playerData.setRemainingTime(remainingTime);
            playerData.setHintUsed(hintUsed);

            String wordStr = props.getProperty("word", "APPLE");
            String defStr = props.getProperty("definition", "");
            Word word = new Word(playerData.getCategory(), playerData.getDifficulty(), wordStr, defStr);
            playerData.setCurrentWord(word);

            HangmanModel model = new HangmanModel(wordStr);
            String guessedStr = props.getProperty("guessedLetters", "");
            if (!guessedStr.trim().isEmpty()) {
                String[] letters = guessedStr.split(",");
                for (String l : letters) {
                    if (!l.trim().isEmpty()) {
                        model.tryLetter(l.trim().charAt(0));
                    }
                }
            }

            return new SavedGameState(playerData, model, word);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class SavedGameState {
        public PlayerData playerData;
        public HangmanModel model;
        public Word word;

        public SavedGameState(PlayerData playerData, HangmanModel model, Word word) {
            this.playerData = playerData;
            this.model = model;
            this.word = word;
        }
    }
}
