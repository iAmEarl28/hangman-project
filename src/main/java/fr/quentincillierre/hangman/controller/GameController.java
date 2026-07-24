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
import fr.quentincillierre.hangman.util.GameSession;
import fr.quentincillierre.hangman.util.SceneManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

public class GameController {

    @FXML
    private Button backButton;
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

    @FXML
    public void initialize() {
        
        if (backButton != null) {
            backButton.setVisible(GameSession.getPlayerData().hasStarted());
            backButton.setOnAction(event -> goBack());
        }

        if (tryAgainButton != null) {
            tryAgainButton.setVisible(false);
            tryAgainButton.setOnAction(event -> startNewRound());
        }

        if (hintButton != null) {
            hintButton.setOnAction(event -> buyHint());
        }

        startNewRound();
    }

    private boolean lastRoundWin;

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

        if (resultLabel != null) {
            resultLabel.setText("");
        }

        if (tryAgainButton != null) {
            tryAgainButton.setText("Continue");
            tryAgainButton.setVisible(false);
        }

        updateHeaderLabels();
        updateWordDisplay();
        updateStatusLabels();
        buildKeyboard();
        updateImage();
        startTimer();
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
        letterButtons.clear();

        // QWERTY layout
        String[] qwertyLayout = {
            "QWERTYUIOP",
            "ASDFGHJKL",
            "ZXCVBNM"
        };

        for (int row = 0; row < qwertyLayout.length; row++) {
            String rowLetters = qwertyLayout[row];
            for (int col = 0; col < rowLetters.length(); col++) {
                String letter = String.valueOf(rowLetters.charAt(col));

                Button letterButton = new Button(letter);
                letterButton.setPrefSize(42, 42);
                letterButton.setOnAction(event -> handleLetterGuess(letterButton, letter.charAt(0)));
                keyboardGrid.add(letterButton, col, row);
                letterButtons.add(letterButton);
            }
        }
    }

    private void updateHeaderLabels() {
        if (categoryLabel != null && currentWord != null) {
            categoryLabel.setText("Category: " + currentWord.getCategory().name());
        }
        if (difficultyLabel != null && currentWord != null) {
            difficultyLabel.setText("Difficulty: " + currentWord.getDifficulty().name());
        }
        if (definitionLabel != null && currentWord != null) {
            definitionLabel.setText("Definition: " + currentWord.getDefinition());
        }
        if (diamondLabel != null) {
            diamondLabel.setText("Diamonds: " + GameSession.getPlayerData().getDiamonds());
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
            attemptsLabel.setText("Attempts: " + playerData.getRemainingAttempts());
        }
        if (streakLabel != null) {
            streakLabel.setText("Streak: " + playerData.getCurrentStreak());
        }
        if (highStreakLabel != null) {
            highStreakLabel.setText("Highest: " + playerData.getHighestStreak());
        }
        if (diamondLabel != null) {
            diamondLabel.setText("Diamonds: " + playerData.getDiamonds());
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
        hangmanImageView.setImage(new Image(getClass().getResourceAsStream(resolveHangmanImagePath(wrongs))));
    }

    public static String resolveHangmanImagePath(int wrongCount) {
        return "/pictures/" + wrongCount + "-hangman.png";
    }

    private void handleWin() {
        if (wordLabel != null) {
            String solvedWord = currentWord != null ? currentWord.getWord() : (model != null ? model.getWordToGuess() : "");
            wordLabel.setText(formatWord(solvedWord));
        }
        if (resultLabel != null) {
            resultLabel.setText("Congratulations! You guessed the word.\n         Do you want to continue?");
        }
        if (tryAgainButton != null) {
            tryAgainButton.setText("Continue");
            tryAgainButton.setVisible(true);
        }
        if (hintButton != null) {
            hintButton.setDisable(true);
        }
        stopTimer();
        PlayerData playerData = GameSession.getPlayerData();
        playerData.addDiamonds(getRewardForDifficulty(playerData.getDifficulty()));
        playerData.increaseStreak();
        lastRoundWin = true;
        updateStatusLabels();
    }

    private void handleLoss() {
        if (wordLabel != null) {
            String solvedWord = currentWord != null ? currentWord.getWord() : (model != null ? model.getWordToGuess() : "");
            wordLabel.setText(formatWord(solvedWord));
        }
        if (resultLabel != null) {
            resultLabel.setText("Game over! Do you want to try again?");
        }
        if (tryAgainButton != null) {
            tryAgainButton.setText("Try Again");
            tryAgainButton.setVisible(true);
        }
        if (hintButton != null) {
            hintButton.setDisable(true);
        }
        stopTimer();
        GameSession.getPlayerData().resetStreak();
        lastRoundWin = false;
        updateStatusLabels();
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

    private void startTimer() {
        stopTimer();
        PlayerData playerData = GameSession.getPlayerData();
        playerData.resetTime();
        if (timerLabel != null) {
            timerLabel.setText(String.valueOf(playerData.getRemainingTime()));
        }

        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            PlayerData data = GameSession.getPlayerData();
            data.decreaseTime();
            if (timerLabel != null) {
                timerLabel.setText(String.valueOf(data.getRemainingTime()));
            }
            if (data.getRemainingTime() <= 0) {
                handleLoss();
            }
        }));
        timerTimeline.setCycleCount(60);
        timerTimeline.play();
    }

    private void stopTimer() {
        if (timerTimeline != null) {
            timerTimeline.stop();
        }
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
        SceneManager.switchScene("character-view.fxml");
    }

    public static void main(String[] args) {
        launchApp();
    }

    private static void launchApp() {
        // noop to keep the class usable in tests and runtime
    }

}