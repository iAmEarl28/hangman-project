package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.util.SceneManager;

public class CharacterController {

    public void boy1Clicked() {
        // We'll save the selected character later
        SceneManager.switchScene("game-view.fxml");
    }

    public void boy2Clicked() {
        SceneManager.switchScene("game-view.fxml");
    }

    public void girl1Clicked() {
        SceneManager.switchScene("game-view.fxml");
    }

    public void girl2Clicked() {
        SceneManager.switchScene("game-view.fxml");
    }

    public void robotClicked() {
        SceneManager.switchScene("game-view.fxml");
    }

}