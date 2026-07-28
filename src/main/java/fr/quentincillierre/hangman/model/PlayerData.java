package fr.quentincillierre.hangman.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PlayerData {

    // ===================================
    // Player Choices
    // ===================================

    private Difficulty difficulty;
    private Category category;
    private String selectedCharacter;
    private boolean started;

    // ===================================
    // Progress
    // ===================================

    private int diamonds;
    private int currentStreak;
    private int highestStreak;

    // ===================================
    // Unlocked Characters
    // ===================================

    /** Keys of characters the player has purchased/unlocked. "boy1" is always free. */
    private Set<String> unlockedCharacters;

    // ===================================
    // Admin Mode
    // ===================================

    private boolean adminMode;

    // ===================================
    // Current Word
    // ===================================

    private Word currentWord;

    // ===================================
    // Round
    // ===================================

    private int remainingAttempts;
    private int remainingTime;
    private boolean hintUsed;

    // ===================================
    // Constructor
    // ===================================

    public PlayerData() {

        diamonds = 0;
        currentStreak = 0;
        highestStreak = 0;
        started = false;
        adminMode = false;

        unlockedCharacters = new HashSet<>();
        unlockedCharacters.add("boy1"); // JOEY-kun is always free

        resetRound();

    }

    // ===================================
    // Reset Round
    // ===================================

    public void resetRound() {

        currentWord = null;

        remainingAttempts = 10;
        resetTime();   // applies correct time for current difficulty (60/30/20s)

        hintUsed = false;

    }

    // ===================================
    // Difficulty
    // ===================================

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        resetTime();
    }

    // ===================================
    // Category
    // ===================================

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    // ===================================
    // Character
    // ===================================

    public String getSelectedCharacter() {
        return selectedCharacter;
    }

    public void setSelectedCharacter(String selectedCharacter) {
        this.selectedCharacter = selectedCharacter;
    }

    public boolean hasStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    // ===================================
    // Unlocked Characters
    // ===================================

    /** Returns true if the character with the given key is unlocked (or admin mode is on). */
    public boolean isCharacterUnlocked(String key) {
        if (adminMode) return true;
        return unlockedCharacters.contains(key);
    }

    /** Permanently unlocks a character for this player. */
    public void unlockCharacter(String key) {
        if (key != null) {
            unlockedCharacters.add(key);
        }
    }

    /** Returns an unmodifiable view of the unlocked character keys (for saving). */
    public Set<String> getUnlockedCharacters() {
        return Collections.unmodifiableSet(unlockedCharacters);
    }

    /** Replaces the full set of unlocked characters (used when loading a save). */
    public void setUnlockedCharacters(Set<String> keys) {
        unlockedCharacters = new HashSet<>(keys);
        unlockedCharacters.add("boy1"); // boy1 always free
    }

    // ===================================
    // Admin Mode
    // ===================================

    public boolean isAdminMode() {
        return adminMode;
    }

    public void setAdminMode(boolean adminMode) {
        this.adminMode = adminMode;
    }

    // ===================================
    // Current Word
    // ===================================


    public Word getCurrentWord() {
        return currentWord;
    }

    public void setCurrentWord(Word currentWord) {
        this.currentWord = currentWord;
    }

    // ===================================
    // Diamonds
    // ===================================

    public int getDiamonds() {
        return diamonds;
    }

    public void setDiamonds(int diamonds) {
        this.diamonds = diamonds;
    }

    public void addDiamonds(int amount) {

        if (amount > 0) {

            diamonds += amount;

        }

    }

    public boolean spendDiamonds(int amount) {

        if (diamonds >= amount) {

            diamonds -= amount;
            return true;

        }

        return false;

    }

    // ===================================
    // Streak
    // ===================================

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getHighestStreak() {
        return highestStreak;
    }

    public void setHighestStreak(int highestStreak) {
        this.highestStreak = highestStreak;
    }

    public void increaseStreak() {

        currentStreak++;

        if (currentStreak > highestStreak) {

            highestStreak = currentStreak;

        }

    }

    public void resetStreak() {

        currentStreak = 0;

    }

    // ===================================
    // Attempts
    // ===================================

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public void setRemainingAttempts(int remainingAttempts) {
        this.remainingAttempts = remainingAttempts;
    }

    public void decreaseAttempt() {

        if (remainingAttempts > 0) {

            remainingAttempts--;

        }

    }

    public void resetAttempts() {

        remainingAttempts = 10;

    }

    // ===================================
    // Timer
    // ===================================

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public void decreaseTime() {

        if (remainingTime > 0) {

            remainingTime--;

        }

    }

    public void resetTime() {
        if (difficulty == Difficulty.MEDIUM) {
            remainingTime = 30;
        } else if (difficulty == Difficulty.HARD) {
            remainingTime = 20;
        } else {
            remainingTime = 60;
        }
    }

    // ===================================
    // Hint
    // ===================================

    public boolean isHintUsed() {
        return hintUsed;
    }

    public void setHintUsed(boolean hintUsed) {
        this.hintUsed = hintUsed;
    }

    public void useHint() {

        hintUsed = true;

    }

    public void resetHint() {

        hintUsed = false;

    }

}