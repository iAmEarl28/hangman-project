package fr.quentincillierre.hangman.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import fr.quentincillierre.hangman.model.Category;
import fr.quentincillierre.hangman.model.Difficulty;
import fr.quentincillierre.hangman.model.HangmanModel;
import fr.quentincillierre.hangman.model.PlayerData;
import fr.quentincillierre.hangman.model.Word;
import fr.quentincillierre.hangman.model.WordRepository;
import fr.quentincillierre.hangman.util.CharacterManager;
import fr.quentincillierre.hangman.util.GameSession;
import fr.quentincillierre.hangman.util.ImageLoader;
import fr.quentincillierre.hangman.util.SaveManager;
import fr.quentincillierre.hangman.util.SceneManager;
import fr.quentincillierre.hangman.util.SoundManager;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class GameController {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private VBox glassCardBox;
    @FXML
    private Button backButton;
    @FXML
    private Button saveExitButton;
    @FXML
    private Button soundButton;
    @FXML
    private Label categoryLabel;
    @FXML
    private Label difficultyLabel;
    @FXML
    private Label diamondLabel;
    @FXML
    private Label timerLabel;
    @FXML
    private ImageView hangmanImageView;
    @FXML
    private ImageView characterPortraitView;
    @FXML
    private Label definitionLabel;
    @FXML
    private Label wordLabel;
    @FXML
    private Button hintButton;
    @FXML
    private GridPane keyboardGrid;
    @FXML
    private Label attemptsLabel;
    @FXML
    private Label streakLabel;
    @FXML
    private Label highStreakLabel;
    @FXML
    private Label resultLabel;
    @FXML
    private Button tryAgainButton;

    private HangmanModel model;
    private Word currentWord;
    private Timeline timerTimeline;
    private final List<Button> letterButtons = new ArrayList<>();
    private static final String[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");

    /** The currently-visible result/unlock overlay, or null if none is shown. */
    private StackPane currentOverlay;

    /** Callback stored alongside the current overlay so the Enter key can trigger it. */
    private Runnable currentOverlayDismissCallback;

    /** Guard to prevent multiple rapid clicks/Enter presses from triggering duplicate transitions or starting multiple timelines/rounds. */
    private boolean overlayDismissing = false;

    // Fade-out then fade-in when the hangman image changes; ~130ms each way
    // adds up to the requested ~250ms smooth transition.
    private static final Duration IMAGE_FADE_DURATION = Duration.millis(130);
    private static final Duration VICTORY_BOUNCE_DURATION = Duration.millis(220);

    @FXML
    public void initialize() {
        
        if (backButton != null) {
            backButton.setVisible(GameSession.getPlayerData().hasStarted());
            backButton.setOnAction(event -> goBack());
        }

        if (saveExitButton != null) {
            saveExitButton.setOnAction(event -> saveAndExit());
        }

        if (soundButton != null) {
            soundButton.setText(SoundManager.getInstance().isMuted() ? "🔇" : "🔊");
            soundButton.setOnAction(event -> toggleSound());
        }

        SoundManager.getInstance().playBackgroundMusic();

        if (tryAgainButton != null) {
            tryAgainButton.setVisible(false);
            tryAgainButton.setOnAction(event -> startNewRound());
        }

        if (hintButton != null) {
            hintButton.setOnAction(event -> buyHint());
        }

        setupCharacterPortrait();

        if (rootPane != null) {
            rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene == null) {
                    stopTimer();
                }
            });
        }

        if (GameSession.isPendingResume()) {
            restoreSavedRound();
            GameSession.clearPendingResume();
        } else if (GameSession.hasActiveRound()) {
            resumeActiveRound();
        } else {
            startNewRound();
        }

        Platform.runLater(() -> {
            if (rootPane != null && rootPane.getScene() != null) {
                // Use an event FILTER (capture phase) so our handler runs BEFORE
                // any focused Button (e.g. backButton) sees the KeyEvent.
                // This prevents Enter from accidentally triggering goBack().
                rootPane.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::handlePhysicalKeyPress);
            }
        });
    }

    private void handlePhysicalKeyPress(KeyEvent event) {
        // ENTER dismisses the result/unlock overlay and continues.
        // We consume the event so the focused backButton never fires goBack().
        if (event.getCode() == KeyCode.ENTER) {
            event.consume();
            if (currentOverlay != null) {
                Runnable callback = currentOverlayDismissCallback;
                currentOverlayDismissCallback = null;
                removeCurrentOverlay(callback != null ? callback : this::startNewRound);
            }
            return;
        }

        // During active gameplay, consume all key events so no stray button
        // (back, save-exit, try-again) fires via keyboard.
        if (model != null && !model.isWin() && !model.isLose()) {
            // Only let printable A-Z through; everything else is consumed.
            String character = null;
            if (event.getText() != null && event.getText().length() == 1) {
                char ch = Character.toUpperCase(event.getText().charAt(0));
                if (ch >= 'A' && ch <= 'Z') {
                    character = String.valueOf(ch);
                }
            }

            if (character == null && event.getCode() != null) {
                String codeName = event.getCode().name();
                if (codeName.length() == 1) {
                    char ch = Character.toUpperCase(codeName.charAt(0));
                    if (ch >= 'A' && ch <= 'Z') {
                        character = String.valueOf(ch);
                    }
                }
            }

            // Consume the event regardless so buttons don't fire
            event.consume();

            if (character != null) {
                char charToGuess = character.charAt(0);
                for (Button button : letterButtons) {
                    if (button.getText() != null && button.getText().equalsIgnoreCase(character)) {
                        if (!button.isDisabled()) {
                            handleLetterGuess(button, charToGuess);
                        }
                        break;
                    }
                }
            }
        }
    }

    private boolean lastRoundWin;

    /**
     * Shows the selected character's small portrait once when the game
     * screen loads. It stays on screen for the whole session (including
     * across "Try Again" rounds, since this controller instance and its
     * ImageView are reused) - a persistent reminder of who is being hanged.
     */
    private void setupCharacterPortrait() {
        if (characterPortraitView == null) {
            return;
        }
        String characterKey = GameSession.getPlayerData().getSelectedCharacter();
        characterPortraitView.setImage(ImageLoader.load(CharacterManager.resolvePortraitImagePath(characterKey)));
    }

    private void startNewRound() {
        PlayerData playerData = GameSession.getPlayerData();
        playerData.resetRound();
        playerData.resetHint();

        Difficulty difficulty = playerData.getDifficulty() == null ? Difficulty.EASY : playerData.getDifficulty();
        Category category = pickRandomCategory();
        playerData.setCategory(category);

        currentWord = new WordRepository().getRandomWord(category, difficulty);
        if (currentWord == null) {
            currentWord = new Word(category, difficulty, "APPLE", "A sweet fruit.");
        }

        playerData.setCurrentWord(currentWord);
        model = new HangmanModel(currentWord.getWord());
        GameSession.setActiveRound(model, currentWord);

        if (resultLabel != null) {
            resultLabel.setText("");
        }

        if (tryAgainButton != null) {
            tryAgainButton.setText("Continue");
            tryAgainButton.setVisible(false);
        }

        if (hintButton != null) {
            hintButton.setDisable(false);
        }

        if (backButton != null) {
            backButton.setDisable(false);
            backButton.setVisible(GameSession.getPlayerData().hasStarted());
        }

        updateHeaderLabels();
        updateWordDisplay();
        updateStatusLabels();
        buildKeyboard();
        updateImage();
        startTimer(true);
    }

    private void restoreSavedRound() {
        PlayerData playerData = GameSession.getPlayerData();
        currentWord = playerData.getCurrentWord();
        model = GameSession.getRestoredModel();

        if (model == null && currentWord != null) {
            model = new HangmanModel(currentWord.getWord());
        }

        GameSession.setActiveRound(model, currentWord);

        if (resultLabel != null) {
            resultLabel.setText("");
        }

        if (tryAgainButton != null) {
            tryAgainButton.setText("Continue");
            tryAgainButton.setVisible(false);
        }

        if (hintButton != null) {
            hintButton.setDisable(playerData.isHintUsed());
        }

        if (backButton != null) {
            backButton.setDisable(false);
            backButton.setVisible(playerData.hasStarted());
        }

        updateHeaderLabels();
        updateWordDisplay();
        updateStatusLabels();
        buildKeyboard();

        if (model != null && currentWord != null) {
            for (Button btn : letterButtons) {
                if (btn.getText() != null && !btn.getText().isEmpty()) {
                    char letterChar = btn.getText().charAt(0);
                    char lowerLetter = Character.toLowerCase(letterChar);
                    if (model.getGuessedLetter().contains(lowerLetter)) {
                        btn.setDisable(true);
                        if (currentWord.getWord().toLowerCase().contains(String.valueOf(lowerLetter))) {
                            btn.getStyleClass().add("key-correct");
                        } else {
                            btn.getStyleClass().add("key-wrong");
                        }
                    }
                }
            }
        }

        updateImage();
        startTimer(false);
    }

    private void resumeActiveRound() {
        PlayerData playerData = GameSession.getPlayerData();
        currentWord = GameSession.getActiveWord();
        model = GameSession.getActiveModel();

        if (currentWord == null || model == null) {
            startNewRound();
            return;
        }

        if (resultLabel != null) {
            resultLabel.setText("");
        }

        if (tryAgainButton != null) {
            tryAgainButton.setText("Continue");
            tryAgainButton.setVisible(false);
        }

        if (hintButton != null) {
            hintButton.setDisable(playerData.isHintUsed());
        }

        if (backButton != null) {
            backButton.setDisable(false);
            backButton.setVisible(playerData.hasStarted());
        }

        updateHeaderLabels();
        updateWordDisplay();
        updateStatusLabels();
        buildKeyboard();

        if (model != null && currentWord != null) {
            for (Button btn : letterButtons) {
                if (btn.getText() != null && !btn.getText().isEmpty()) {
                    char letterChar = btn.getText().charAt(0);
                    char lowerLetter = Character.toLowerCase(letterChar);
                    if (model.getGuessedLetter().contains(lowerLetter)) {
                        btn.setDisable(true);
                        if (currentWord.getWord().toLowerCase().contains(String.valueOf(lowerLetter))) {
                            btn.getStyleClass().add("key-correct");
                        } else {
                            btn.getStyleClass().add("key-wrong");
                        }
                    }
                }
            }
        }

        updateImage();
        startTimer(false);
    }

    private Category pickRandomCategory() {
        Category[] categories = Category.values();
        return categories[new Random().nextInt(categories.length)];
    }

    private void buildKeyboard() {
        if (keyboardGrid == null) {
            return;
        }

        keyboardGrid.getChildren().clear();
        keyboardGrid.getColumnConstraints().clear();
        letterButtons.clear();

        // QWERTY layout
        String[] qwertyLayout = {
            "QWERTYUIOP",
            "ASDFGHJKL",
            "ZXCVBNM"
        };

        // Every key spans two "half columns" so that shorter rows (ASDF.../ZXCV...)
        // can be offset by an odd number of half-columns and end up perfectly
        // centered under the longest row, just like a real keyboard.
        int longestRow = 0;
        for (String rowLetters : qwertyLayout) {
            longestRow = Math.max(longestRow, rowLetters.length());
        }
        int totalHalfColumns = longestRow * 2;

        for (int i = 0; i < totalHalfColumns; i++) {
            javafx.scene.layout.ColumnConstraints columnConstraints = new javafx.scene.layout.ColumnConstraints();
            columnConstraints.setPrefWidth(23);
            keyboardGrid.getColumnConstraints().add(columnConstraints);
        }

        for (int row = 0; row < qwertyLayout.length; row++) {
            String rowLetters = qwertyLayout[row];
            int rowOffset = (totalHalfColumns - rowLetters.length() * 2) / 2;

            for (int col = 0; col < rowLetters.length(); col++) {
                String letter = String.valueOf(rowLetters.charAt(col));

                Button letterButton = new Button(letter);
                letterButton.getStyleClass().add("key-button");
                letterButton.setPrefSize(44, 44);
                letterButton.setMaxSize(44, 44);
                letterButton.setOnAction(event -> handleLetterGuess(letterButton, letter.charAt(0)));

                int startColumn = rowOffset + col * 2;
                keyboardGrid.add(letterButton, startColumn, row, 2, 1);
                letterButtons.add(letterButton);
            }
        }
    }

    private void updateHeaderLabels() {
        if (categoryLabel != null && currentWord != null) {
            categoryLabel.setText("📖 " + currentWord.getCategory().name());
        }
        if (difficultyLabel != null && currentWord != null) {
            difficultyLabel.setText("🎯 " + currentWord.getDifficulty().name());
        }
        if (definitionLabel != null && currentWord != null) {
            definitionLabel.setText(currentWord.getDefinition());
        }
        if (diamondLabel != null) {
            diamondLabel.setText("💎 " + GameSession.getPlayerData().getDiamonds());
        }
    }

    private void updateWordDisplay() {
        if (wordLabel == null || model == null) {
            return;
        }

        String hiddenWord = model.getHiddenWord();
        StringBuilder display = new StringBuilder();
        for (int i = 0; i < hiddenWord.length(); i++) {
            if (i > 0) {
                display.append(' ');
            }
            display.append(hiddenWord.charAt(i));
        }
        wordLabel.setText(display.toString());
    }

    private void updateStatusLabels() {
        PlayerData playerData = GameSession.getPlayerData();
        if (attemptsLabel != null) {
            attemptsLabel.setText("💗 Attempts: " + playerData.getRemainingAttempts());
        }
        if (streakLabel != null) {
            streakLabel.setText("🔥 Streak: " + playerData.getCurrentStreak());
        }
        if (highStreakLabel != null) {
            highStreakLabel.setText("🏆 Best: " + playerData.getHighestStreak());
        }
        if (diamondLabel != null) {
            diamondLabel.setText("💎 " + playerData.getDiamonds());
        }
    }

    private void handleLetterGuess(Button button, char letter) {
        if (model == null || button == null) {
            return;
        }

        if (model.isWin() || model.isLose()) {
            return;
        }

        int previousWrongs = model.getCurrentWrongs();
        model.tryLetter(letter);
        button.setDisable(true);

        if (model.getCurrentWrongs() > previousWrongs) {
            GameSession.getPlayerData().decreaseAttempt();
            button.getStyleClass().add("key-wrong");
        } else {
            button.getStyleClass().add("key-correct");
        }

        updateWordDisplay();
        updateStatusLabels();
        updateImage();

        if (model.isWin()) {
            handleWin();
        } else if (model.isLose() || GameSession.getPlayerData().getRemainingAttempts() <= 0) {
            handleLoss();
        }
    }

    private void updateImage() {
        if (hangmanImageView == null) {
            return;
        }

        int wrongs = model == null ? 0 : model.getCurrentWrongs();
        setHangmanImage(resolveHangmanImagePath(GameSession.getPlayerData().getSelectedCharacter(), wrongs));
    }

    /**
     * Swaps the hangman image with a short cross-fade (~250ms total) instead
     * of an abrupt replace. All image loading goes through ImageLoader, and
     * all path-building goes through CharacterManager, so this is the one
     * place that actually touches the ImageView.
     */
    private void setHangmanImage(String classpathImagePath) {
        setHangmanImage(classpathImagePath, null);
    }

    private void setHangmanImage(String classpathImagePath, Runnable onShown) {
        if (hangmanImageView == null) {
            return;
        }

        Image newImage = ImageLoader.load(classpathImagePath);

        FadeTransition fadeOut = new FadeTransition(IMAGE_FADE_DURATION, hangmanImageView);
        fadeOut.setFromValue(hangmanImageView.getOpacity());
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(fadeOutEvent -> {
            hangmanImageView.setImage(newImage);

            FadeTransition fadeIn = new FadeTransition(IMAGE_FADE_DURATION, hangmanImageView);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            if (onShown != null) {
                fadeIn.setOnFinished(fadeInEvent -> onShown.run());
            }
            fadeIn.play();
        });
        fadeOut.play();
    }

    /**
     * Small bounce/scale pulse played once the winning pose has faded in, to
     * make the win feel more rewarding.
     */
    private void playVictoryBounce() {
        if (hangmanImageView == null) {
            return;
        }

        ScaleTransition bounce = new ScaleTransition(VICTORY_BOUNCE_DURATION, hangmanImageView);
        bounce.setFromX(0.85);
        bounce.setFromY(0.85);
        bounce.setToX(1.1);
        bounce.setToY(1.1);
        bounce.setCycleCount(2);
        bounce.setAutoReverse(true);
        bounce.play();
    }

    /**
     * Resolves the classpath path to the hangman stage picture (0-hangman.png
     * ... 10-hangman.png) for the given character key. Delegates to
     * CharacterManager, which maps each character key to its own
     * resources/pictures/{folder} directory.
     */
    public static String resolveHangmanImagePath(String characterKey, int wrongCount) {
        return CharacterManager.resolveStageImagePath(characterKey, wrongCount);
    }

    private void handleWin() {
        if (hintButton != null) hintButton.setDisable(true);
        if (tryAgainButton != null) tryAgainButton.setVisible(false);
        if (resultLabel != null) resultLabel.setText("");

        stopTimer();
        SaveManager.deleteSaveFile();
        GameSession.clearActiveRound();
        PlayerData playerData = GameSession.getPlayerData();
        setHangmanImage(CharacterManager.resolveWinningPoseImagePath(playerData.getSelectedCharacter()), this::playVictoryBounce);
        playerData.addDiamonds(getRewardForDifficulty(playerData.getDifficulty()));

        // Character bonus diamonds (LON +1, JOBOY +2, KAREN +3, JOHNNY +5)
        int bonus = CharacterManager.getBonusDiamonds(playerData.getSelectedCharacter());
        playerData.addDiamonds(bonus);

        int previousHighest = playerData.getHighestStreak();
        playerData.increaseStreak();
        int newHighest = playerData.getHighestStreak();

        lastRoundWin = true;
        updateStatusLabels();

        String solvedWord = currentWord != null ? currentWord.getWord() : (model != null ? model.getWordToGuess() : "");

        // Also reveal word in wordLabel so it's visible even without an overlay (and for tests)
        if (wordLabel != null) {
            wordLabel.setText(formatWord(solvedWord));
        }

        if (previousHighest < 10 && newHighest >= 10) {
            Platform.runLater(() -> showResultOverlay(true, solvedWord, () -> promptUnlockNewLevel(Difficulty.MEDIUM)));
        } else if (previousHighest < 20 && newHighest >= 20) {
            Platform.runLater(() -> showResultOverlay(true, solvedWord, () -> promptUnlockNewLevel(Difficulty.HARD)));
        } else {
            Platform.runLater(() -> showResultOverlay(true, solvedWord, null));
        }
    }

    /**
     * Shows a beautiful glassmorphic win or lose overlay card on the game screen.
     * @param isWin       true for win (green/gold card), false for loss (pink/rose card)
     * @param solvedWord  the word that was guessed (or the answer on loss)
     * @param onAfterDismiss  optional callback run after the overlay is dismissed (e.g. unlock prompt)
     */
    @FXML
    private void toggleSound() {
        boolean isMuted = SoundManager.getInstance().toggleMute();
        if (soundButton != null) {
            soundButton.setText(isMuted ? "🔇" : "🔊");
        }
    }

    private void showResultOverlay(boolean isWin, String solvedWord, Runnable onAfterDismiss) {
        // Reset overlayDismissing flag so the button can be clicked
        overlayDismissing = false;

        // Store the callback so the Enter key can also trigger it
        currentOverlayDismissCallback = (onAfterDismiss != null) ? onAfterDismiss : this::startNewRound;

        if (isWin) {
            SoundManager.getInstance().playCorrectSound();
        } else {
            SoundManager.getInstance().playFailedSound();
        }

        if (rootPane == null) {
            if (onAfterDismiss != null) onAfterDismiss.run();
            else startNewRound();
            return;
        }

        // Backdrop sized to cover right panel area
        StackPane backdrop = new StackPane();
        backdrop.setStyle("-fx-background-color: transparent;");

        Runnable updateAnchors = () -> {
            double cardTop    = 70;
            double cardRight  = 40;
            double cardBottom = 170;
            double cardLeft   = 390;

            if (glassCardBox != null && glassCardBox.getScene() != null && glassCardBox.getWidth() > 0 && rootPane != null && rootPane.getScene() != null) {
                try {
                    Bounds boundsInScene = glassCardBox.localToScene(glassCardBox.getBoundsInLocal());
                    Bounds boundsInRoot  = rootPane.sceneToLocal(boundsInScene);
                    cardTop    = boundsInRoot.getMinY();
                    cardLeft   = boundsInRoot.getMinX();
                    cardBottom = Math.max(0, rootPane.getHeight() - boundsInRoot.getMaxY());
                    cardRight  = Math.max(0, rootPane.getWidth()  - boundsInRoot.getMaxX());
                } catch (Exception ignored) {
                }
            }

            AnchorPane.setTopAnchor(backdrop,    cardTop);
            AnchorPane.setBottomAnchor(backdrop, cardBottom);
            AnchorPane.setLeftAnchor(backdrop,   cardLeft);
            AnchorPane.setRightAnchor(backdrop,  cardRight);
        };

        updateAnchors.run();
        Platform.runLater(updateAnchors);

        // Card sized appropriately for the right panel area
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(400);
        card.setMaxWidth(420);
        card.setMaxHeight(400);
        StackPane.setAlignment(card, Pos.CENTER);
        card.setStyle(
            "-fx-background-radius: 22; " +
            "-fx-border-radius: 22; " +
            "-fx-border-width: 3; " +
            "-fx-padding: 16 20 16 20; " +
            (isWin
                ? "-fx-background-color: linear-gradient(to bottom, #FFFDE7, #FFF8C1, #E8F8E8); -fx-border-color: #A8D88A;"
                : "-fx-background-color: linear-gradient(to bottom, #FFF0F8, #FFE0F0, #F8E0FF); -fx-border-color: #E8A0C8;") +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.38), 32, 0.3, 0, 6);"
        );

        // Emoji
        Label emoji = new Label(isWin ? "🎉" : "😢");
        emoji.setStyle("-fx-font-size: 38px;");

        // Title
        Label title = new Label(isWin ? "Congratulations!" : "Game Over!");
        title.setStyle(
            "-fx-font-size: 20px; -fx-font-weight: bold; " +
            (isWin ? "-fx-text-fill: #3A7A1E;" : "-fx-text-fill: #A22A64;") +
            "-fx-font-family: 'Comic Sans MS', 'Segoe Print', sans-serif;"
        );

        // Subtitle
        Label subtitle = new Label(isWin ? "You guessed it! ✨" : "The word was:");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666; -fx-font-style: italic;");

        // Revealed word pill
        Label wordPill = new Label(formatWord(solvedWord));
        wordPill.setStyle(
            "-fx-font-size: 18px; -fx-font-weight: bold; " +
            "-fx-padding: 6 20 6 20; " +
            "-fx-background-radius: 24; -fx-border-radius: 24; -fx-border-width: 2; " +
            (isWin
                ? "-fx-background-color: linear-gradient(to right, #C6F2D3, #8FE0AC); -fx-border-color: #6FCB93; -fx-text-fill: #1F5C36;"
                : "-fx-background-color: linear-gradient(to right, #FFD6E6, #FFB6D8); -fx-border-color: #E885B0; -fx-text-fill: #8A1A50;") +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 4, 0.15, 0, 2);"
        );

        // Stats row (streak + diamonds)
        PlayerData pd = GameSession.getPlayerData();
        HBox statsRow = new HBox(12);
        statsRow.setAlignment(Pos.CENTER);
        Label streakStat = new Label("🔥 Streak: " + pd.getCurrentStreak());
        streakStat.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #B05020; -fx-background-color: rgba(255,200,100,0.3); -fx-padding: 3 10 3 10; -fx-background-radius: 12;");
        Label diamondStat = new Label("💎 " + pd.getDiamonds());
        diamondStat.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1D5E80; -fx-background-color: rgba(166,227,255,0.4); -fx-padding: 3 10 3 10; -fx-background-radius: 12;");
        statsRow.getChildren().addAll(streakStat, diamondStat);

        // Action button
        Button actionBtn = new Button(isWin ? "▶  Continue" : "🔄  Try Again");
        actionBtn.setStyle(
            "-fx-background-radius: 20; -fx-border-radius: 20; -fx-border-width: 2; " +
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; " +
            "-fx-padding: 8 26 8 26; " +
            (isWin
                ? "-fx-background-color: linear-gradient(to bottom, #7DE898, #4CAF72); -fx-border-color: #FFFFFF; -fx-text-fill: #FFFFFF; -fx-effect: dropshadow(gaussian,rgba(52,150,90,0.5),8,0.3,0,2);"
                : "-fx-background-color: linear-gradient(to bottom, #FF90C0, #E8569A); -fx-border-color: #FFFFFF; -fx-text-fill: #FFFFFF; -fx-effect: dropshadow(gaussian,rgba(220,60,130,0.5),8,0.3,0,2);")
        );

        // Hint: press ENTER too
        Label enterHint = new Label("Press Enter to continue");
        enterHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888; -fx-font-style: italic;");

        card.getChildren().addAll(emoji, title, subtitle, wordPill, statsRow, actionBtn, enterHint);
        backdrop.getChildren().add(card);

        // Animate in
        backdrop.setOpacity(0);
        card.setScaleX(0.7);
        card.setScaleY(0.7);
        rootPane.getChildren().add(backdrop);
        currentOverlay = backdrop;

        FadeTransition backdropFade = new FadeTransition(Duration.millis(220), backdrop);
        backdropFade.setFromValue(0); backdropFade.setToValue(1);

        ScaleTransition cardPop = new ScaleTransition(Duration.millis(320), card);
        cardPop.setFromX(0.7); cardPop.setFromY(0.7);
        cardPop.setToX(1.05); cardPop.setToY(1.05);

        ScaleTransition cardSettle = new ScaleTransition(Duration.millis(110), card);
        cardSettle.setFromX(1.05); cardSettle.setFromY(1.05);
        cardSettle.setToX(1.0);    cardSettle.setToY(1.0);

        new ParallelTransition(backdropFade, new SequentialTransition(cardPop, cardSettle)).play();

        actionBtn.setOnAction(e -> {
            currentOverlayDismissCallback = null;
            if (onAfterDismiss != null) {
                removeCurrentOverlay(onAfterDismiss);
            } else {
                removeCurrentOverlay(this::startNewRound);
            }
        });

        // Hover effects for action button
        actionBtn.setOnMouseEntered(e -> actionBtn.setStyle(actionBtn.getStyle().replace("-fx-background-radius",
            "-fx-scale-x: 1.06; -fx-scale-y: 1.06; -fx-background-radius")));
    }

    private void removeCurrentOverlay(Runnable afterRemove) {
        if (overlayDismissing) {
            return;
        }
        overlayDismissing = true;

        if (currentOverlay == null) {
            if (afterRemove != null) afterRemove.run();
            return;
        }
        StackPane overlay = currentOverlay;
        currentOverlay = null;
        FadeTransition ft = new FadeTransition(Duration.millis(200), overlay);
        ft.setFromValue(overlay.getOpacity()); ft.setToValue(0);
        ft.setOnFinished(e -> {
            rootPane.getChildren().remove(overlay);
            if (afterRemove != null) afterRemove.run();
        });
        ft.play();
    }

    private void promptUnlockNewLevel(Difficulty newDifficulty) {
        if (rootPane == null) return;
        String levelName = (newDifficulty == Difficulty.MEDIUM) ? "Medium Mode" : "Hard Mode";
        String unlockColor = (newDifficulty == Difficulty.MEDIUM)
            ? "linear-gradient(to bottom, #FFF9E6, #FFF0B0, #FFDFA0)"
            : "linear-gradient(to bottom, #FFE8E8, #FFC8C8, #FFB0B0)";
        String unlockBorder = (newDifficulty == Difficulty.MEDIUM) ? "#FFCC44" : "#FF7A7A";
        String unlockTextColor = (newDifficulty == Difficulty.MEDIUM) ? "#7A5000" : "#8A1010";

        // Backdrop
        StackPane backdrop = new StackPane();
        backdrop.setStyle("-fx-background-color: rgba(20,10,30,0.60);");
        AnchorPane.setTopAnchor(backdrop, 0.0);
        AnchorPane.setBottomAnchor(backdrop, 0.0);
        AnchorPane.setLeftAnchor(backdrop, 0.0);
        AnchorPane.setRightAnchor(backdrop, 0.0);

        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(420);
        card.setMaxWidth(440);
        card.setMaxHeight(380);
        StackPane.setAlignment(card, Pos.CENTER);
        card.setStyle(
            "-fx-background-radius: 32; -fx-border-radius: 32; -fx-border-width: 3; -fx-padding: 36 40 32 40; " +
            "-fx-background-color: " + unlockColor + "; -fx-border-color: " + unlockBorder + "; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.40), 44, 0.3, 0, 10);"
        );

        Label trophyEmoji = new Label("🏆");
        trophyEmoji.setStyle("-fx-font-size: 56px;");

        Label unlockTitle = new Label("Level Unlocked! 🎉");
        unlockTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + unlockTextColor + "; -fx-font-family: 'Comic Sans MS','Segoe Print',sans-serif;");

        Label unlockMsg = new Label("You reached a 10-streak!\nYou've unlocked " + levelName + "!");
        unlockMsg.setWrapText(true);
        unlockMsg.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555; -fx-text-alignment: center; -fx-alignment: center;");
        unlockMsg.setAlignment(Pos.CENTER);

        Label levelPill = new Label("✨  " + levelName + "  ✨");
        levelPill.setStyle(
            "-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 8 28 8 28; " +
            "-fx-background-radius: 28; -fx-border-radius: 28; -fx-border-width: 2; " +
            "-fx-background-color: rgba(255,255,255,0.7); -fx-border-color: " + unlockBorder + "; -fx-text-fill: " + unlockTextColor + ";"
        );

        HBox buttons = new HBox(14);
        buttons.setAlignment(Pos.CENTER);

        Button enterBtn = new Button("▶  Enter " + levelName);
        enterBtn.setMinWidth(190);
        enterBtn.setStyle(
            "-fx-background-radius: 22; -fx-border-radius: 22; -fx-border-width: 2; -fx-border-color: #FFFFFF; " +
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 9 24 9 24; " +
            "-fx-background-color: linear-gradient(to bottom, #FFB84A, #FF8A00); -fx-text-fill: #FFFFFF; " +
            "-fx-effect: dropshadow(gaussian,rgba(200,100,0,0.5),10,0.3,0,3);"
        );

        Button stayBtn = new Button("Continue Current");
        stayBtn.setMinWidth(155);
        stayBtn.setStyle(
            "-fx-background-radius: 22; -fx-border-radius: 22; -fx-border-width: 2; -fx-border-color: #CCCCCC; " +
            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 9 20 9 20; " +
            "-fx-background-color: linear-gradient(to bottom, #F0F0F0, #DDDDDD); -fx-text-fill: #555555;"
        );

        buttons.getChildren().addAll(enterBtn, stayBtn);
        card.getChildren().addAll(trophyEmoji, unlockTitle, unlockMsg, levelPill, buttons);
        backdrop.getChildren().add(card);

        // Animate in
        backdrop.setOpacity(0);
        card.setScaleX(0.6); card.setScaleY(0.6);
        rootPane.getChildren().add(backdrop);
        currentOverlay = backdrop;

        FadeTransition backdropFade = new FadeTransition(Duration.millis(250), backdrop);
        backdropFade.setFromValue(0); backdropFade.setToValue(1);
        ScaleTransition cardPop = new ScaleTransition(Duration.millis(360), card);
        cardPop.setFromX(0.6); cardPop.setFromY(0.6);
        cardPop.setToX(1.06); cardPop.setToY(1.06);
        ScaleTransition settle = new ScaleTransition(Duration.millis(120), card);
        settle.setFromX(1.06); settle.setFromY(1.06);
        settle.setToX(1.0);    settle.setToY(1.0);
        new ParallelTransition(backdropFade, new SequentialTransition(cardPop, settle)).play();

        enterBtn.setOnAction(e -> {
            if (overlayDismissing) return;
            overlayDismissing = true;
            currentOverlay = null;
            currentOverlayDismissCallback = null;
            FadeTransition ft = new FadeTransition(Duration.millis(180), backdrop);
            ft.setFromValue(1); ft.setToValue(0);
            ft.setOnFinished(ev -> {
                rootPane.getChildren().remove(backdrop);
                GameSession.getPlayerData().setDifficulty(newDifficulty);
                updateHeaderLabels();
                playWelcomeAnimation("\u2728 WELCOME TO " + levelName.toUpperCase() + " \u2728", this::startNewRound);
            });
            ft.play();
        });

        stayBtn.setOnAction(e -> {
            currentOverlayDismissCallback = null;
            removeCurrentOverlay(this::startNewRound);
        });
    }

    private void playWelcomeAnimation(String message, Runnable onFinished) {
        if (rootPane == null) {
            if (onFinished != null) onFinished.run();
            return;
        }

        // Choose colors/content based on difficulty
        PlayerData pd = GameSession.getPlayerData();
        boolean isMedium = pd.getDifficulty() == Difficulty.MEDIUM;
        String bgGradient = isMedium
            ? "linear-gradient(to bottom right, #7B2FF7, #F107A3)"
            : "linear-gradient(to bottom right, #FF416C, #FF4B2B)";
        String badgeColor = isMedium ? "#FFD700" : "#FF8C00";
        String timeTip = isMedium ? "\u23F1  30 seconds per word" : "\u23F1  20 seconds per word";

        // Full-screen curtain
        StackPane curtain = new StackPane();
        curtain.setStyle("-fx-background-color: " + bgGradient + ";");
        AnchorPane.setTopAnchor(curtain, 0.0);
        AnchorPane.setBottomAnchor(curtain, 0.0);
        AnchorPane.setLeftAnchor(curtain, 0.0);
        AnchorPane.setRightAnchor(curtain, 0.0);
        curtain.setOpacity(0);

        // Content card floating inside the curtain
        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
            "-fx-background-color: rgba(255,255,255,0.15); " +
            "-fx-background-radius: 28; " +
            "-fx-padding: 40 60 40 60;"
        );
        card.setTranslateY(60);
        card.setOpacity(0);

        Label iconLabel = new Label(isMedium ? "\u2B50" : "\uD83D\uDD25");
        iconLabel.setStyle("-fx-font-size: 64px;");

        Label welcomeLabel = new Label("WELCOME TO");
        welcomeLabel.setStyle(
            "-fx-font-size: 15px; -fx-text-fill: rgba(255,255,255,0.85); " +
            "-fx-font-weight: bold;"
        );

        Label levelLabel = new Label(isMedium ? "MEDIUM MODE" : "HARD MODE");
        levelLabel.setStyle(
            "-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF; " +
            "-fx-font-family: 'Comic Sans MS','Segoe Print',sans-serif; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 10, 0.5, 0, 3);"
        );

        Label tipLabel = new Label(timeTip);
        tipLabel.setStyle(
            "-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.9); " +
            "-fx-background-color: rgba(255,255,255,0.20); " +
            "-fx-padding: 6 20 6 20; -fx-background-radius: 20;"
        );

        Label getReadyLabel = new Label("Get Ready!");
        getReadyLabel.setStyle(
            "-fx-font-size: 17px; -fx-text-fill: " + badgeColor + "; -fx-font-weight: bold;"
        );

        card.getChildren().addAll(iconLabel, welcomeLabel, levelLabel, tipLabel, getReadyLabel);
        curtain.getChildren().add(card);
        rootPane.getChildren().add(curtain);

        // Phase 1: Curtain fades in + card slides up
        FadeTransition curtainFadeIn = new FadeTransition(Duration.millis(380), curtain);
        curtainFadeIn.setFromValue(0); curtainFadeIn.setToValue(1);

        TranslateTransition cardSlideUp = new TranslateTransition(Duration.millis(480), card);
        cardSlideUp.setFromY(60); cardSlideUp.setToY(0);
        FadeTransition cardFadeIn = new FadeTransition(Duration.millis(480), card);
        cardFadeIn.setFromValue(0); cardFadeIn.setToValue(1);
        ParallelTransition phaseIn = new ParallelTransition(
            curtainFadeIn, new ParallelTransition(cardSlideUp, cardFadeIn)
        );

        // Phase 2: Hold
        PauseTransition hold = new PauseTransition(Duration.millis(1500));

        // Phase 3: Curtain slides upward off screen
        TranslateTransition curtainOut = new TranslateTransition(Duration.millis(520), curtain);
        curtainOut.setFromY(0); curtainOut.setToY(-800);
        FadeTransition curtainFadeOut = new FadeTransition(Duration.millis(520), curtain);
        curtainFadeOut.setFromValue(1); curtainFadeOut.setToValue(0);
        ParallelTransition phaseOut = new ParallelTransition(curtainOut, curtainFadeOut);

        SequentialTransition seq = new SequentialTransition(phaseIn, hold, phaseOut);
        seq.setOnFinished(e -> {
            rootPane.getChildren().remove(curtain);
            if (onFinished != null) onFinished.run();
        });
        seq.play();
    }

    private void handleLoss() {
        if (hintButton != null) hintButton.setDisable(true);
        if (tryAgainButton != null) tryAgainButton.setVisible(false);
        if (resultLabel != null) resultLabel.setText("");

        stopTimer();
        SaveManager.deleteSaveFile();
        GameSession.clearActiveRound();
        PlayerData playerData = GameSession.getPlayerData();
        setHangmanImage(CharacterManager.resolveDefeatImagePath(playerData.getSelectedCharacter()));
        playerData.resetStreak();
        lastRoundWin = false;
        updateStatusLabels();

        String solvedWord = currentWord != null ? currentWord.getWord() : (model != null ? model.getWordToGuess() : "");

        // Also reveal word in wordLabel so it's visible even without an overlay (and for tests)
        if (wordLabel != null) {
            wordLabel.setText(formatWord(solvedWord));
        }

        Platform.runLater(() -> showResultOverlay(false, solvedWord, null));
    }

    private String formatWord(String word) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            if (i > 0) {
                builder.append(' ');
            }
            builder.append(word.charAt(i));
        }
        return builder.toString();
    }

    private void updateTimerDisplay(int remainingSeconds) {
        if (timerLabel != null) {
            timerLabel.setText(String.valueOf(remainingSeconds));
            if (remainingSeconds <= 5) {
                if (!timerLabel.getStyleClass().contains("timer-warning")) {
                    timerLabel.getStyleClass().add("timer-warning");
                }
            } else {
                timerLabel.getStyleClass().remove("timer-warning");
            }
        }
    }

    private void startTimer(boolean resetTime) {
        stopTimer();
        PlayerData playerData = GameSession.getPlayerData();
        if (resetTime) {
            playerData.resetTime();
        }
        int remainingSeconds = playerData.getRemainingTime();
        updateTimerDisplay(remainingSeconds);

        if (remainingSeconds <= 0) {
            handleLoss();
            return;
        }

        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            PlayerData data = GameSession.getPlayerData();
            if (model == null || model.isWin() || model.isLose()) {
                stopTimer();
                return;
            }
            data.decreaseTime();
            updateTimerDisplay(data.getRemainingTime());
            if (data.getRemainingTime() <= 0) {
                stopTimer();
                handleLoss();
            }
        }));
        timerTimeline.setCycleCount(remainingSeconds);
        timerTimeline.play();
    }

    private void stopTimer() {
        if (timerTimeline != null) {
            timerTimeline.stop();
            timerTimeline = null;
        }
    }

    @FXML
    private void saveAndExit() {
        stopTimer();
        if (model != null && !model.isWin() && !model.isLose()) {
            SaveManager.saveGame(GameSession.getPlayerData(), model, currentWord);
        }
        SceneManager.switchScene("start-view.fxml");
    }

    private int getRewardForDifficulty(Difficulty difficulty) {
        if (difficulty == Difficulty.MEDIUM) {
            return 2;
        }
        if (difficulty == Difficulty.HARD) {
            return 3;
        }
        return 1;
    }

    private void buyHint() {
        PlayerData playerData = GameSession.getPlayerData();
        if (playerData.isHintUsed()) {
            return;
        }

        if (playerData.getDiamonds() < 10) {
            showInsufficientDiamondsAlert();
            return;
        }

        if (!playerData.spendDiamonds(10)) {
            showInsufficientDiamondsAlert();
            return;
        }

        playerData.useHint();
        if (hintButton != null) {
            hintButton.setDisable(true);
        }

        if (model == null) {
            return;
        }

        String word = currentWord.getWord();
        for (int i = 0; i < word.length(); i++) {
            char character = word.charAt(i);
            if (!model.getGuessedLetter().contains(Character.toLowerCase(character))) {
                model.tryLetter(character);
                break;
            }
        }

        updateWordDisplay();
        updateStatusLabels();
        if (model.isWin()) {
            handleWin();
        }
    }

    private Alert createInsufficientDiamondsAlert() {
        if (Platform.isFxApplicationThread()) {
            return buildInsufficientDiamondsAlert();
        }

        FutureTask<Alert> alertTask = new FutureTask<>(this::buildInsufficientDiamondsAlert);
        Platform.runLater(alertTask);
        try {
            return alertTask.get(5, TimeUnit.SECONDS);
        } catch (Exception exception) {
            throw new RuntimeException("Unable to create the insufficient diamonds alert", exception);
        }
    }

    private Alert buildInsufficientDiamondsAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Insufficient Diamonds");
        alert.setHeaderText(null);
        alert.setContentText("You need at least 10 diamonds to buy a hint.");
        return alert;
    }

    private void showInsufficientDiamondsAlert() {
        if (Platform.isFxApplicationThread()) {
            createInsufficientDiamondsAlert().showAndWait();
            return;
        }

        Platform.runLater(() -> createInsufficientDiamondsAlert().showAndWait());
    }

    @FXML
    private void goBack() {
        stopTimer();
        SceneManager.switchScene("character-view.fxml");
    }

    public static void main(String[] args) {
        launchApp();
    }

    private static void launchApp() {
        // noop to keep the class usable in tests and runtime
    }

}