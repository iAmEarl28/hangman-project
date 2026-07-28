package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.model.Difficulty;
import fr.quentincillierre.hangman.model.PlayerData;
import fr.quentincillierre.hangman.util.GameSession;
import fr.quentincillierre.hangman.util.SaveManager;
import fr.quentincillierre.hangman.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

public class DifficultyController {

    private static final int MEDIUM_STREAK_REQUIRED = 10;
    private static final int HARD_STREAK_REQUIRED = 20;

    @FXML
    private Button backButton;

    @FXML
    private Button easyButton;

    @FXML
    private Button mediumButton;

    @FXML
    private Button hardButton;

    @FXML
    public void initialize() {
        if (backButton != null) {
            backButton.setVisible(GameSession.getPlayerData().hasStarted());
        }

        updateDifficultyButtonLabels();
    }

    private void updateDifficultyButtonLabels() {
        PlayerData playerData = GameSession.getPlayerData();
        int highestStreak = playerData != null ? playerData.getHighestStreak() : 0;
        boolean admin = playerData != null && playerData.isAdminMode();
        Difficulty currentDiff = playerData != null ? playerData.getDifficulty() : null;

        if (easyButton != null) {
            if (currentDiff == Difficulty.EASY) {
                easyButton.setText("🌼 Easy  ✓");
            } else {
                easyButton.setText("🌼 Easy");
            }
            easyButton.setOpacity(1.0);
        }

        if (mediumButton != null) {
            if (admin || highestStreak >= MEDIUM_STREAK_REQUIRED) {
                if (currentDiff == Difficulty.MEDIUM) {
                    mediumButton.setText("🌟 Medium  ✓");
                } else {
                    mediumButton.setText("🌟 Medium");
                }
                mediumButton.setOpacity(1.0);
            } else {
                mediumButton.setText("🔒 Medium (Req: 10 Streak)");
                mediumButton.setOpacity(0.75);
            }
        }

        if (hardButton != null) {
            if (admin || highestStreak >= HARD_STREAK_REQUIRED) {
                if (currentDiff == Difficulty.HARD) {
                    hardButton.setText("🔥 Hard  ✓");
                } else {
                    hardButton.setText("🔥 Hard");
                }
                hardButton.setOpacity(1.0);
            } else {
                hardButton.setText("🔒 Hard (Req: 20 Streak)");
                hardButton.setOpacity(0.75);
            }
        }
    }

    private void selectDifficulty(Difficulty targetDifficulty) {
        PlayerData playerData = GameSession.getPlayerData();
        if (playerData != null) {
            Difficulty current = playerData.getDifficulty();
            if (current != targetDifficulty) {
                // Changing difficulty level clears any active round from previous level
                GameSession.clearActiveRound();
                SaveManager.deleteSaveFile();
            }
            playerData.setDifficulty(targetDifficulty);
        }
        SceneManager.switchScene("character-view.fxml");
    }

    @FXML
    public void easyClicked() {
        selectDifficulty(Difficulty.EASY);
    }

    @FXML
    public void easyClicked(ActionEvent event) {
        selectDifficulty(Difficulty.EASY);
    }

    @FXML
    public void mediumClicked() {
        PlayerData playerData = GameSession.getPlayerData();
        if (playerData != null && !playerData.isAdminMode()
                && playerData.getHighestStreak() < MEDIUM_STREAK_REQUIRED) {
            showLockedAlert("Medium Mode", MEDIUM_STREAK_REQUIRED);
            return;
        }

        selectDifficulty(Difficulty.MEDIUM);
    }

    @FXML
    public void mediumClicked(ActionEvent event) {
        mediumClicked();
    }

    @FXML
    public void hardClicked() {
        PlayerData playerData = GameSession.getPlayerData();
        if (playerData != null && !playerData.isAdminMode()
                && playerData.getHighestStreak() < HARD_STREAK_REQUIRED) {
            showLockedAlert("Hard Mode", HARD_STREAK_REQUIRED);
            return;
        }

        selectDifficulty(Difficulty.HARD);
    }

    @FXML
    public void hardClicked(ActionEvent event) {
        hardClicked();
    }

    private void showLockedAlert(String levelName, int streakRequired) {
        PlayerData playerData = GameSession.getPlayerData();
        int currentStreak = playerData != null ? playerData.getHighestStreak() : 0;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(levelName + " Locked");
        alert.setHeaderText("🔒 " + levelName + " Requires " + streakRequired + " Streak!");
        alert.setContentText(
            "Your highest streak is currently " + currentStreak + ".\n" +
            "You need a " + streakRequired + "-streak to unlock " + levelName + "!\n\n" +
            "💡 Tip: Play Easy Mode to build your streak, or enter Admin Password 'JLJKJ12345' on the Start screen to unlock all levels!"
        );
        alert.showAndWait();
    }

    @FXML
    public void goBack() {
        SceneManager.switchScene("start-view.fxml");
    }

    @FXML
    public void goBack(ActionEvent event) {
        goBack();
    }

}