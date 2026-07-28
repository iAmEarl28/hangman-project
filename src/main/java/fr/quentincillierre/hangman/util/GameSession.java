package fr.quentincillierre.hangman.util;

import fr.quentincillierre.hangman.model.HangmanModel;
import fr.quentincillierre.hangman.model.PlayerData;
import fr.quentincillierre.hangman.model.Word;

public final class GameSession {

    private static PlayerData playerData = new PlayerData();
    private static boolean pendingResume = false;
    private static HangmanModel activeModel = null;
    private static Word activeWord = null;

    private GameSession() {

    }

    public static PlayerData getPlayerData() {
        return playerData;
    }

    public static void newGame() {
        playerData = new PlayerData();
        pendingResume = false;
        activeModel = null;
        activeWord = null;
    }

    public static boolean isPendingResume() {
        return pendingResume;
    }

    public static void clearPendingResume() {
        pendingResume = false;
    }

    public static HangmanModel getActiveModel() {
        return activeModel;
    }

    public static Word getActiveWord() {
        return activeWord;
    }

    public static boolean hasActiveRound() {
        return activeModel != null && activeWord != null && !activeModel.isWin() && !activeModel.isLose();
    }

    public static void setActiveRound(HangmanModel model, Word word) {
        activeModel = model;
        activeWord = word;
    }

    public static void clearActiveRound() {
        activeModel = null;
        activeWord = null;
    }

    public static HangmanModel getRestoredModel() {
        return activeModel;
    }

    public static void restoreSession(SaveManager.SavedGameState state) {
        if (state != null) {
            playerData = state.playerData;
            activeModel = state.model;
            activeWord = state.word;
            pendingResume = true;
        }
    }

}