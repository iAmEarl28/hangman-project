package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.model.PlayerData;
import fr.quentincillierre.hangman.util.GameSession;
import fr.quentincillierre.hangman.util.SaveManager;
import fr.quentincillierre.hangman.util.SceneManager;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class StartController {

    private static final String ADMIN_PASSWORD = "JLJKJ12345";

    @FXML
    private Button startButton;
    @FXML
    private Button resumeButton;
    @FXML
    private Button adminButton;
    @FXML
    private HBox adminPanel;
    @FXML
    private PasswordField adminPasswordField;
    @FXML
    private Button adminSubmitButton;
    @FXML
    private Label adminStatusLabel;

    @FXML
    public void initialize() {
        if (resumeButton != null) {
            boolean hasSave = SaveManager.hasSaveFile();
            resumeButton.setDisable(!hasSave);
        }

        // If admin mode is already active (from a previous activation), show indicator
        PlayerData playerData = GameSession.getPlayerData();
        if (playerData.isAdminMode() && adminButton != null) {
            adminButton.setText("✅");
            adminButton.setStyle("-fx-text-fill: #2ECC71;");
        }
    }

    @FXML
    public void startGame() {
        GameSession.getPlayerData().setStarted(true);
        SceneManager.switchScene("difficulty-view.fxml");
    }

    @FXML
    public void resumeGame() {
        if (SaveManager.hasSaveFile()) {
            SaveManager.SavedGameState state = SaveManager.loadGame();
            if (state != null) {
                GameSession.restoreSession(state);
                SceneManager.switchScene("game-view.fxml");
            }
        }
    }

    @FXML
    public void toggleAdminPanel() {
        if (adminPanel != null) {
            boolean nowVisible = !adminPanel.isVisible();
            adminPanel.setVisible(nowVisible);

            // Clear the field when toggling
            if (adminPasswordField != null) {
                adminPasswordField.clear();
            }
            if (adminStatusLabel != null) {
                adminStatusLabel.setVisible(false);
            }
        }
    }

    @FXML
    public void submitAdminPassword() {
        if (adminPasswordField == null || adminStatusLabel == null) return;

        String input = adminPasswordField.getText();

        if (ADMIN_PASSWORD.equals(input)) {
            // Activate admin mode
            PlayerData playerData = GameSession.getPlayerData();
            playerData.setAdminMode(true);

            // Unlock all characters
            playerData.unlockCharacter("boy1");
            playerData.unlockCharacter("girl1");
            playerData.unlockCharacter("boy2");
            playerData.unlockCharacter("girl2");
            playerData.unlockCharacter("robot");

            // Show success feedback
            adminStatusLabel.setText("✅ Admin Mode Activated!");
            adminStatusLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2ECC71;");
            adminStatusLabel.setVisible(true);

            // Update the admin button to show active state
            if (adminButton != null) {
                adminButton.setText("✅");
                adminButton.setStyle("-fx-text-fill: #2ECC71;");
            }

            // Hide panel after a short delay
            PauseTransition delay = new PauseTransition(Duration.millis(1500));
            delay.setOnFinished(e -> {
                if (adminPanel != null) adminPanel.setVisible(false);
            });
            delay.play();
        } else {
            // Wrong password
            adminStatusLabel.setText("❌ Wrong password!");
            adminStatusLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #CC3333;");
            adminStatusLabel.setVisible(true);

            adminPasswordField.clear();

            PauseTransition delay = new PauseTransition(Duration.millis(2000));
            delay.setOnFinished(e -> {
                if (adminStatusLabel != null) adminStatusLabel.setVisible(false);
            });
            delay.play();
        }
    }

}