package fr.quentincillierre.hangman.controller; // tells Java this class belongs in the controller package

// ===== Java Imports =====
import java.io.InputStream; // used to read data from files (like words.txt)
import java.util.HashMap; // stores data as key → value pairs
import java.util.Map; // general type for key → value collections
import java.util.Objects; // helper class for checking objects (e.g., null)

// ===== Your Project Classes =====
import fr.quentincillierre.hangman.model.HangmanModel; // contains the game logic and rules
import fr.quentincillierre.hangman.model.WordRepository; // provides random words for the game

// ===== JavaFX Animation =====
import javafx.animation.KeyFrame; // code that runs at a specific time
import javafx.animation.Timeline; // repeats actions over time (used for the timer)

// ===== JavaFX FXML =====
import javafx.fxml.FXML; // lets FXMLLoader connect private fields and methods

// ===== JavaFX UI Controls =====
import javafx.scene.control.Button; // creates and controls buttons
import javafx.scene.control.Label; // displays text on the screen

// ===== JavaFX Images =====
import javafx.scene.image.Image; // represents an image file
import javafx.scene.image.ImageView; // displays an image in the UI

// ===== JavaFX Layout =====
import javafx.scene.layout.GridPane; // arranges nodes in rows and columns

// ===== JavaFX Time =====
import javafx.util.Duration; // represents a length of time (e.g., 1 second)

public class GameController {

    @FXML
    private ImageView hangmanImageView; //

    @FXML
    private Label wordLabel; //

    @FXML
    private Label resultLabel; //

    @FXML
    private Label timerLabel;

    @FXML
    private Label attemptsLabel;

    @FXML
    private Label streakLabel;

    @FXML
    private Label highStreakLabel;

    @FXML
    private GridPane keyboardGrid; //

    @FXML
    private Button tryAgainButton;

    private HangmanModel model;
    private final WordRepository wordRepository = new WordRepository(); //

    private int currentStreak = 0;
    private int highestStreak = 0;
    private boolean isGameOver = false;

    private Timeline timerTimeline;
    private int secondsRemaining = 60;

    private final Map<String, Button> buttonMap = new HashMap<>();

    @FXML
    public void initialize() { //
        generateKeyboard(); //
        startNewGame();
    }

    private void startNewGame() {
        isGameOver = false;
        secondsRemaining = 60;
        resultLabel.setText("");
        wordLabel.setText(""); // Clear previous word display
        tryAgainButton.setVisible(false);

        String targetWord = wordRepository.getRandomWord();
        model = new HangmanModel(targetWord);

        buttonMap.values().forEach(button -> {
            button.setDisable(false);
            button.setStyle(
                    "-fx-background-color: #3a3a4a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; -fx-background-radius: 8; -fx-cursor: hand;");
        });

        if (timerTimeline != null) {
            timerTimeline.stop();
        }
        setupTimer();
        refreshUI(); //
    }

    private void setupTimer() {
        timerLabel.setText("01:00");
        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            secondsRemaining--;
            int mins = secondsRemaining / 60;
            int secs = secondsRemaining % 60;
            timerLabel.setText(String.format("%02d:%02d", mins, secs));

            if (secondsRemaining <= 10) {
                timerLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 20;");
            } else {
                timerLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold; -fx-font-size: 20;");
            }

            if (secondsRemaining <= 0) {
                handleTimeOut();
            }
        }));
        timerTimeline.setCycleCount(Timeline.INDEFINITE);
        timerTimeline.play();
    }

    private void handleTimeOut() {
        stopTimer();
        isGameOver = true;
        currentStreak = 0;

        resultLabel.setText("⏰ TIME'S UP! Game Over!");
        resultLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        wordLabel.setText(formatWordDisplay(model.getWordToGuess()));
        disableEntireKeyboard();
        tryAgainButton.setVisible(true);
    }

    public void refreshUI() { //
        wordLabel.setText(formatWordDisplay(model.getHiddenWord())); //

        int attemptsLeft = 10 - model.getCurrentWrongs();
        attemptsLabel.setText(attemptsLeft + " / 10");

        if (attemptsLeft <= 3) {
            attemptsLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 20;");
        } else {
            attemptsLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 20;");
        }

        // --- SMART IMAGE LOADING LOGIC ---
        int wrongCount = model.getCurrentWrongs();
        try {
            String imagePath = resolveHangmanImagePath(wrongCount);
            InputStream is = getClass().getResourceAsStream(imagePath);

            if (is != null) {
                hangmanImageView.setImage(new Image(is));
            } else {
                System.err.println("Could not locate any hangman image files in /pictures/ for index: " + wrongCount);
            }
        } catch (Exception e) {
            System.err.println("Error loading hangman image asset: " + e.getMessage());
        }

        streakLabel.setText(String.valueOf(currentStreak));
        highStreakLabel.setText(String.valueOf(highestStreak));

        if (model.isWin()) { //
            handleWin();
        } else if (model.isLose()) {
            handleLoss();
        }
    }

    private void handleWin() {
        stopTimer();
        isGameOver = true;
        currentStreak++;
        if (currentStreak > highestStreak) {
            highestStreak = currentStreak;
        }

        resultLabel.setText("🏆 WINNER! STREAK: " + currentStreak + " 🔥");
        resultLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 24;");
        wordLabel.setText(formatWordDisplay(model.getWordToGuess()));
        disableEntireKeyboard();
        tryAgainButton.setVisible(true);
    }

    private void handleLoss() {
        stopTimer();
        isGameOver = true;
        currentStreak = 0;

        String fullWord = model.getWordToGuess();
        String formattedWord = formatWordDisplay(fullWord);

        resultLabel.setText("💀 GAME OVER! Word: " + fullWord);
        resultLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 24;");

        wordLabel.setText(formattedWord);
        disableEntireKeyboard();
        tryAgainButton.setVisible(true);
    }

    @FXML
    private void handleTryAgain() {
        startNewGame();
    }

    private void generateKeyboard() { //
        keyboardGrid.getChildren().clear();
        int columns = 9;

        for (char ch = 'A'; ch <= 'Z'; ch++) { //
            String letter = String.valueOf(ch);
            Button button = new Button(letter); //
            button.setPrefSize(55, 45);
            button.setStyle(
                    "-fx-background-color: #3a3a4a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; -fx-background-radius: 8; -fx-cursor: hand;");

            button.setOnAction(event -> { //
                handleKeyboardInput(letter);
            });

            int index = ch - 'A';
            int row = index / columns;
            int col = index % columns;

            keyboardGrid.add(button, col, row); //
            buttonMap.put(letter, button);
        }
    }

    public void handleKeyboardInput(String character) { //
        if (isGameOver) {
            return;
        }

        String letter = character.toUpperCase();

        if (letter.matches("[A-Z]") && buttonMap.containsKey(letter)) {
            Button associatedButton = buttonMap.get(letter);

            if (!associatedButton.isDisable()) {
                model.tryLetter(letter.charAt(0)); //
                associatedButton.setDisable(true);

                if (model.getWordToGuess().toUpperCase().contains(letter)) {
                    associatedButton
                            .setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
                } else {
                    associatedButton
                            .setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
                }

                refreshUI(); //
            }
        }
    }

    private void disableEntireKeyboard() {
        buttonMap.values().forEach(button -> button.setDisable(true));
    }

    private void stopTimer() {
        if (timerTimeline != null) {
            timerTimeline.stop();
        }
    }

    static String resolveHangmanImagePath(int wrongCount) {
        String primaryPath = "/pictures/" + wrongCount + "-hangman.png";
        String fallbackPath = "/pictures/" + wrongCount + ".png";

        return Objects.requireNonNullElseGet(
                GameController.class.getResource(primaryPath),
                () -> GameController.class.getResource(fallbackPath)) == null ? fallbackPath : primaryPath;
    }

    private String formatWordDisplay(String word) {
        if (word == null || word.isEmpty()) {
            return "";
        }

        StringBuilder display = new StringBuilder();
        String normalizedWord = word.toUpperCase().trim();

        for (int i = 0; i < normalizedWord.length(); i++) {
            if (i > 0) {
                display.append(" ");
            }
            display.append(normalizedWord.charAt(i));
        }

        return display.toString();
    }
}