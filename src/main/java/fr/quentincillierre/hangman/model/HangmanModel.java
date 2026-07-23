package fr.quentincillierre.hangman.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Model class for the Hangman game managing the state and core logic.
 */
public class HangmanModel {

    // Attributes required by the session specifications [cite: 33, 34, 35, 36]
    private final String wordToGuess;
    private final int maxWrongs = 10; // Standard maximum wrong guesses allowed before losing [cite: 34]
    private int currentWrongs;
    private final Set<Character> guessedLetter; // Holds lowercase letters that have been guessed [cite: 36]

    /**
     * Constructor to initialize the game state[cite: 39, 40].
     * * @param wordToGuess The word the player needs to guess.
     */
    public HangmanModel(String wordToGuess) {
        if (wordToGuess == null || wordToGuess.trim().isEmpty()) {
            throw new IllegalArgumentException("Word to guess cannot be null or empty.");
        }
        this.wordToGuess = wordToGuess;
        this.currentWrongs = 0;
        this.guessedLetter = new HashSet<>();
    }

    /**
     * Returns the set of letters that have already been guessed[cite: 43, 44].
     */
    public Set<Character> getGuessedLetter() {
        return this.guessedLetter;
    }

    /**
     * Returns the current number of wrong guesses[cite: 45, 46].
     */
    public int getCurrentWrongs() {
        return this.currentWrongs;
    }

    /**
     * Returns the word the player needs to guess[cite: 48, 49].
     */
    public String getWordToGuess() {
        return this.wordToGuess;
    }

    /**
     * Handles a letter guess: checks if the letter is correct, updates the game state, 
     * and tracks guessed letters[cite: 50, 51]. Must be case-insensitive.
     * * @param letter The character guessed by the player.
     */
    public void tryLetter(Character letter) {
        if (letter == null || !Character.isLetter(letter)) {
            return;
        }

        // Handle case-insensitivity by converting the guessed letter to lowercase 
        char lowerLetter = Character.toLowerCase(letter);

        // Process only if the letter hasn't been guessed yet
        if (!guessedLetter.contains(lowerLetter)) {
            guessedLetter.add(lowerLetter);

            // Check if the letter is part of the word to guess
            if (!wordToGuess.toLowerCase().contains(String.valueOf(lowerLetter))) {
                currentWrongs++;
            }
        }
    }

    /**
     * Returns the word to guess with unguessed letters replaced by underscores ('_')[cite: 52, 53].
     * No extra spaces are added between letters to match test specifications.
     */
    public String getHiddenWord() {
        StringBuilder hidden = new StringBuilder();
        for (char c : wordToGuess.toCharArray()) {
            if (guessedLetter.contains(Character.toLowerCase(c))) {
                hidden.append(c);
            } else {
                hidden.append('_');
            }
        }
        return hidden.toString();
    }

    /**
     * Returns true if the game is won (all characters guessed)[cite: 54, 55].
     */
    public boolean isWin() {
        for (char c : wordToGuess.toCharArray()) {
            if (!guessedLetter.contains(Character.toLowerCase(c))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the game is lost (limit of wrong guesses reached)[cite: 56, 57].
     */
    public boolean isLose() {
        return currentWrongs >= maxWrongs;
    }
}