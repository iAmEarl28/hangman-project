package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.model.Difficulty;
import fr.quentincillierre.hangman.util.GameSession;
import fr.quentincillierre.hangman.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class DifficultyController {

    @FXML
    private Button backButton;

    @FXML
    public void initialize() {
        if (backButton != null) {
            backButton.setVisible(GameSession.getPlayerData().hasStarted());
        }
    }

    public void easyClicked() {

        GameSession.getPlayerData().setDifficulty(Difficulty.EASY);

        SceneManager.switchScene("character-view.fxml");
    }

    public void mediumClicked() {

        GameSession.getPlayerData().setDifficulty(Difficulty.MEDIUM);

        SceneManager.switchScene("character-view.fxml");
    }

    public void hardClicked() {

        GameSession.getPlayerData().setDifficulty(Difficulty.HARD);

        SceneManager.switchScene("character-view.fxml");
    }

    @FXML
    private void goBack() {
        SceneManager.switchScene("start-view.fxml");
    }

}