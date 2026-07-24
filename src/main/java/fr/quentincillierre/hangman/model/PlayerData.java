package fr.quentincillierre.hangman.model;

public class PlayerData {

    // ===================================
    // Player Choices
    // ===================================

    private Difficulty difficulty;
    private Category category;
    private String selectedCharacter;

    // ===================================
    // Progress
    // ===================================

    private int diamonds;
    private int currentStreak;
    private int highestStreak;

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

        resetRound();

    }

    // ===================================
    // Reset Round
    // ===================================

    public void resetRound() {

        currentWord = null;

        remainingAttempts = 6;
        remainingTime = 60;

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

    public int getHighestStreak() {
        return highestStreak;
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

    public void decreaseAttempt() {

        if (remainingAttempts > 0) {

            remainingAttempts--;

        }

    }

    public void resetAttempts() {

        remainingAttempts = 6;

    }

    // ===================================
    // Timer
    // ===================================

    public int getRemainingTime() {
        return remainingTime;
    }

    public void decreaseTime() {

        if (remainingTime > 0) {

            remainingTime--;

        }

    }

    public void resetTime() {

        remainingTime = 60;

    }

    // ===================================
    // Hint
    // ===================================

    public boolean isHintUsed() {
        return hintUsed;
    }

    public void useHint() {

        hintUsed = true;

    }

    public void resetHint() {

        hintUsed = false;

    }

}