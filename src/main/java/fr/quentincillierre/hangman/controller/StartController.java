package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.util.SceneManager;

public class StartController {

    public void startGame() {

        SceneManager.switchScene("difficulty-view.fxml");

    }

}