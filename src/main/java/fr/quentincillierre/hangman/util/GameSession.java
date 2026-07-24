package fr.quentincillierre.hangman.util;

import fr.quentincillierre.hangman.model.PlayerData;

public final class GameSession {

    private static PlayerData playerData = new PlayerData();

    private GameSession() {

    }

    public static PlayerData getPlayerData() {
        return playerData;
    }

    public static void newGame() {

        playerData = new PlayerData();

    }

}