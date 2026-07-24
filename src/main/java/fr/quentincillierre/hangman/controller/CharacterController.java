package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.util.GameSession;
import fr.quentincillierre.hangman.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class CharacterController {

    @FXML
    private Button backButton;

    @FXML
    public void initialize() {
        if (backButton != null) {
            backButton.setVisible(GameSession.getPlayerData().hasStarted());
        }
    }

    public void boy1Clicked() {
        selectCharacter("Boy 1");
    }

    public void boy2Clicked() {
        selectCharacter("Boy 2");
    }

    public void girl1Clicked() {
        selectCharacter("Girl 1");
    }

    public void girl2Clicked() {
        selectCharacter("Girl 2");
    }

    public void robotClicked() {
        selectCharacter("Robot");
    }

    private void selectCharacter(String characterName) {
        GameSession.getPlayerData().setSelectedCharacter(characterName);
        SceneManager.switchScene("game-view.fxml");
    }

    @FXML
    private void goBack() {
        SceneManager.switchScene("difficulty-view.fxml");
    }

}