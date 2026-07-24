package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.model.Difficulty;
import fr.quentincillierre.hangman.util.GameSession;
import fr.quentincillierre.hangman.util.SceneManager;

public class DifficultyController {

    public void easyClicked() {

        GameSession.getPlayerData().setDifficulty(Difficulty.EASY);

        SceneManager.switchScene("category-view.fxml");
    }

    public void mediumClicked() {

        GameSession.getPlayerData().setDifficulty(Difficulty.MEDIUM);

        SceneManager.switchScene("category-view.fxml");
    }

    public void hardClicked() {

        GameSession.getPlayerData().setDifficulty(Difficulty.HARD);

        SceneManager.switchScene("category-view.fxml");
    }

}