package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.model.PlayerData;
import fr.quentincillierre.hangman.util.CharacterManager;
import fr.quentincillierre.hangman.util.GameSession;
import fr.quentincillierre.hangman.util.SaveManager;
import fr.quentincillierre.hangman.util.SceneManager;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class CharacterController {

    // Short pause so the player actually sees the pink glow highlight
    // before the game screen loads.
    private static final Duration SELECTION_HIGHLIGHT_DELAY = Duration.millis(280);

    @FXML
    private Button backButton;
    @FXML
    private Button boy1Button;
    @FXML
    private Button girl1Button;
    @FXML
    private Button boy2Button;
    @FXML
    private Button girl2Button;
    @FXML
    private Button robotButton;

    @FXML
    private Label boy1DefLabel;
    @FXML
    private Label girl1DefLabel;
    @FXML
    private Label boy2DefLabel;
    @FXML
    private Label girl2DefLabel;
    @FXML
    private Label robotDefLabel;

    @FXML
    private ImageView boy1Img;
    @FXML
    private ImageView girl1Img;
    @FXML
    private ImageView boy2Img;
    @FXML
    private ImageView girl2Img;
    @FXML
    private ImageView robotImg;

    @FXML
    private Label diamondDisplayLabel;

    /** Currently-visible buy overlay, or null. */
    private StackPane buyOverlay;

    @FXML
    public void initialize() {
        if (backButton != null) {
            backButton.setVisible(GameSession.getPlayerData().hasStarted());
        }

        // Re-highlight currently selected character
        highlightButtonForCharacter(GameSession.getPlayerData().getSelectedCharacter());

        // Update diamond display
        updateDiamondDisplay();

        // Apply lock visuals (shadow silhouettes for locked, full color for unlocked)
        applyLockVisuals();
    }

    private void updateDiamondDisplay() {
        if (diamondDisplayLabel != null) {
            diamondDisplayLabel.setText("💎 " + GameSession.getPlayerData().getDiamonds());
        }
    }

    /**
     * For each character, if unlocked: show full color image artwork.
     * If locked: show solid dark shadow silhouette (brightness = -1.0) and display price/attribute on button.
     */
    private void applyLockVisuals() {
        PlayerData playerData = GameSession.getPlayerData();

        applyLockToCharacter("boy1",  boy1Button, boy1Img,  boy1DefLabel,  playerData);
        applyLockToCharacter("girl1", girl1Button, girl1Img, girl1DefLabel, playerData);
        applyLockToCharacter("boy2",  boy2Button, boy2Img,  boy2DefLabel,  playerData);
        applyLockToCharacter("girl2", girl2Button, girl2Img, girl2DefLabel, playerData);
        applyLockToCharacter("robot", robotButton, robotImg, robotDefLabel, playerData);
    }

    private void applyLockToCharacter(String key, Button button, ImageView img, Label defLabel, PlayerData playerData) {
        if (button == null) return;

        String name = CharacterManager.getName(key);
        int price = CharacterManager.getPrice(key);
        int bonus = CharacterManager.getBonusDiamonds(key);
        boolean unlocked = playerData.isCharacterUnlocked(key);

        // Update trailer definition label below button
        if (defLabel != null) {
            defLabel.setText(CharacterManager.getDefinition(key));
        }

        if (unlocked || price == 0) {
            // Character is unlocked — full color artwork!
            if (img != null) {
                img.setVisible(true);
                img.setEffect(null);
            }
            button.setText(name + "\n" + (price == 0 ? "FREE" : "UNLOCKED") + "\nBonus: +" + bonus + " 💎");
            button.setOpacity(1.0);
        } else {
            // Character is locked — pitch-black shadow silhouette!
            if (img != null) {
                img.setVisible(true);
                ColorAdjust shadow = new ColorAdjust();
                shadow.setBrightness(-1.0); // Solid dark silhouette shape
                img.setEffect(shadow);
            }
            button.setText("🔒 " + name + "\n💎 " + price + "\nBonus: +" + bonus + " 💎");
            button.setOpacity(0.85);
        }
    }

    @FXML
    private void boy1Clicked(ActionEvent event) {
        handleCharacterClick("boy1", boy1Button);
    }

    @FXML
    private void boy2Clicked(ActionEvent event) {
        handleCharacterClick("boy2", boy2Button);
    }

    @FXML
    private void girl1Clicked(ActionEvent event) {
        handleCharacterClick("girl1", girl1Button);
    }

    @FXML
    private void girl2Clicked(ActionEvent event) {
        handleCharacterClick("girl2", girl2Button);
    }

    @FXML
    private void robotClicked(ActionEvent event) {
        handleCharacterClick("robot", robotButton);
    }

    /**
     * Handles a character button click.
     * 1. If unlocked -> select character.
     * 2. If locked -> check sequential unlock requirement (previous character must be unlocked).
     * 3. If previous character locked -> show alert that previous character must be unlocked first.
     * 4. If previous character unlocked, check diamond count -> if insufficient, show alert with exact missing diamonds.
     * 5. If sufficient diamonds -> show buy overlay.
     */
    private void handleCharacterClick(String characterKey, Button selectedButton) {
        PlayerData playerData = GameSession.getPlayerData();

        if (playerData.isCharacterUnlocked(characterKey)) {
            selectCharacter(characterKey, selectedButton);
            return;
        }

        // Check sequential order requirement
        String prevKey = CharacterManager.getPreviousCharacterKey(characterKey);
        if (prevKey != null && !playerData.isCharacterUnlocked(prevKey)) {
            String prevName = CharacterManager.getName(prevKey);
            String currentName = CharacterManager.getName(characterKey);

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Sequential Unlock Required");
            alert.setHeaderText("🔒 Unlock Previous Character First!");
            alert.setContentText("You must unlock " + prevName + " before unlocking " + currentName + "!");
            alert.showAndWait();
            return;
        }

        // Check diamond balance
        int price = CharacterManager.getPrice(characterKey);
        int currentDiamonds = playerData.getDiamonds();

        if (currentDiamonds < price) {
            int missing = price - currentDiamonds;
            String charName = CharacterManager.getName(characterKey);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Insufficient Diamonds");
            alert.setHeaderText("💎 Need More Diamonds!");
            alert.setContentText(
                "You need " + missing + " more diamond" + (missing > 1 ? "s" : "") + " to buy " + charName + "!\n\n" +
                "• Your Diamonds: 💎 " + currentDiamonds + "\n" +
                "• Required: 💎 " + price + "\n" +
                "• Still Needed: 💎 " + missing + " 💎"
            );
            alert.showAndWait();
            return;
        }

        // Show purchase confirmation dialog
        showBuyOverlay(characterKey, selectedButton);
    }

    /**
     * Shows a glassmorphic buy confirmation overlay.
     */
    private void showBuyOverlay(String characterKey, Button selectedButton) {
        dismissBuyOverlay();

        PlayerData playerData = GameSession.getPlayerData();
        int price = CharacterManager.getPrice(characterKey);
        String charName = CharacterManager.getName(characterKey);
        int bonus = CharacterManager.getBonusDiamonds(characterKey);

        AnchorPane root = (AnchorPane) selectedButton.getScene().getRoot();

        StackPane backdrop = new StackPane();
        backdrop.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
        AnchorPane.setTopAnchor(backdrop, 0.0);
        AnchorPane.setBottomAnchor(backdrop, 0.0);
        AnchorPane.setLeftAnchor(backdrop, 0.0);
        AnchorPane.setRightAnchor(backdrop, 0.0);

        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(24, 32, 24, 32));
        card.setMaxWidth(340);
        card.setMaxHeight(240);
        card.setStyle(
            "-fx-background-color: rgba(255,255,255,0.95);" +
            "-fx-background-radius: 22;" +
            "-fx-border-radius: 22;" +
            "-fx-border-color: rgba(255,193,222,0.9);" +
            "-fx-border-width: 2;" +
            "-fx-effect: dropshadow(gaussian, rgba(168,50,121,0.3), 20, 0.3, 0, 5);"
        );

        Label titleLabel = new Label("Unlock " + charName + "?");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #A83279;");

        Label defLabel = new Label("\"" + CharacterManager.getDefinition(characterKey) + "\"");
        defLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7A4B72; -fx-font-style: italic; -fx-text-alignment: center;");
        defLabel.setWrapText(true);

        Label infoLabel = new Label("Price: 💎 " + price + "   |   Bonus on Win: +" + bonus + " 💎");
        infoLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1D5E80;");

        Label balanceLabel = new Label("Your balance: 💎 " + playerData.getDiamonds());
        balanceLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2ECC71; -fx-font-weight: bold;");

        HBox buttonRow = new HBox(14);
        buttonRow.setAlignment(Pos.CENTER);

        Button buyBtn = new Button("Unlock 💎 " + price);
        buyBtn.getStyleClass().addAll("primary-button");
        buyBtn.setPrefWidth(140);
        buyBtn.setPrefHeight(42);
        buyBtn.setStyle(buyBtn.getStyle() + "-fx-font-size: 14px;");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().addAll("secondary-button");
        cancelBtn.setPrefWidth(100);
        cancelBtn.setPrefHeight(42);
        cancelBtn.setStyle(cancelBtn.getStyle() + "-fx-font-size: 14px;");

        buyBtn.setOnAction(e -> {
            if (playerData.spendDiamonds(price)) {
                playerData.unlockCharacter(characterKey);
                dismissBuyOverlay();
                updateDiamondDisplay();
                applyLockVisuals();
                selectCharacter(characterKey, selectedButton);
            }
        });

        cancelBtn.setOnAction(e -> dismissBuyOverlay());

        buttonRow.getChildren().addAll(buyBtn, cancelBtn);
        card.getChildren().addAll(titleLabel, defLabel, infoLabel, balanceLabel, buttonRow);
        backdrop.getChildren().add(card);

        backdrop.setOnMouseClicked(e -> {
            if (e.getTarget() == backdrop) dismissBuyOverlay();
        });

        buyOverlay = backdrop;
        root.getChildren().add(backdrop);
    }

    private void dismissBuyOverlay() {
        if (buyOverlay != null && buyOverlay.getParent() instanceof AnchorPane) {
            ((AnchorPane) buyOverlay.getParent()).getChildren().remove(buyOverlay);
            buyOverlay = null;
        }
    }

    /**
     * Stores the selected character's key on the shared PlayerData.
     * Applies glowing highlight effect to the selected character button.
     */
    private void selectCharacter(String characterKey, Button selectedButton) {
        PlayerData playerData = GameSession.getPlayerData();
        String previousCharacter = playerData.getSelectedCharacter();

        if (previousCharacter != null && !previousCharacter.equalsIgnoreCase(characterKey)) {
            // Reset streak and clear active round if switching character mid-game
            playerData.resetStreak();
            GameSession.clearActiveRound();
            SaveManager.deleteSaveFile();
        }

        playerData.setSelectedCharacter(characterKey);
        clearAllHighlights();
        applyHighlight(selectedButton);

        PauseTransition delay = new PauseTransition(SELECTION_HIGHLIGHT_DELAY);
        delay.setOnFinished(event -> SceneManager.switchScene("game-view.fxml"));
        delay.play();
    }

    private void highlightButtonForCharacter(String characterKey) {
        if (characterKey == null) {
            return;
        }
        applyHighlight(buttonForCharacter(characterKey));
    }

    private Button buttonForCharacter(String characterKey) {
        switch (characterKey) {
            case "boy1":
                return boy1Button;
            case "girl1":
                return girl1Button;
            case "boy2":
                return boy2Button;
            case "girl2":
                return girl2Button;
            case "robot":
                return robotButton;
            default:
                return null;
        }
    }

    private void clearAllHighlights() {
        for (Button button : new Button[] {boy1Button, girl1Button, boy2Button, girl2Button, robotButton}) {
            if (button != null) {
                button.setEffect(null);
                button.getStyleClass().remove("character-card-selected");
            }
        }
    }

    private void applyHighlight(Button button) {
        if (button == null) {
            return;
        }

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#FF4FA3"));
        glow.setRadius(28);
        glow.setSpread(0.55);
        button.setEffect(glow);

        if (!button.getStyleClass().contains("character-card-selected")) {
            button.getStyleClass().add("character-card-selected");
        }
    }

    @FXML
    private void goBack() {
        SceneManager.switchScene("difficulty-view.fxml");
    }

}
