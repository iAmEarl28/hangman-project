package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.util.GameSession;
import fr.quentincillierre.hangman.util.SceneManager;


public class StartController {

    public void startGame() {

        GameSession.getPlayerData().setStarted(true);
        SceneManager.switchScene("difficulty-view.fxml");

    }

}