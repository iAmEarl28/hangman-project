package fr.quentincillierre.hangman.controller;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import fr.quentincillierre.hangman.model.HangmanModel;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

class GameControllerImageTest {

    @BeforeAll
    static void initJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void resolveHangmanImagePathShouldUseClasspathPicturesFolder() {
        assertEquals("/pictures/0-hangman.png", GameController.resolveHangmanImagePath(0));
        assertEquals("/pictures/3-hangman.png", GameController.resolveHangmanImagePath(3));
        assertEquals("/pictures/10-hangman.png", GameController.resolveHangmanImagePath(10));
    }

    @Test
    void winShouldRevealTheFullWord() throws Exception {
        GameController controller = new GameController();
        setField(controller, "model", new HangmanModel("JAVA"));
        setField(controller, "wordLabel", new Label());
        setField(controller, "resultLabel", new Label());
        setField(controller, "timerLabel", new Label());
        setField(controller, "attemptsLabel", new Label());
        setField(controller, "streakLabel", new Label());
        setField(controller, "highStreakLabel", new Label());
        setField(controller, "hangmanImageView", new ImageView());
        setField(controller, "keyboardGrid", new GridPane());
        setField(controller, "tryAgainButton", new Button());

        Method handleWin = GameController.class.getDeclaredMethod("handleWin");
        handleWin.setAccessible(true);
        handleWin.invoke(controller);

        Label wordLabel = (Label) getField(controller, "wordLabel");
        assertEquals("J A V A", wordLabel.getText());
    }

    @Test
    void lossShouldRevealTheFullWord() throws Exception {
        GameController controller = new GameController();
        setField(controller, "model", new HangmanModel("CONCILIA"));
        setField(controller, "wordLabel", new Label());
        setField(controller, "resultLabel", new Label());
        setField(controller, "timerLabel", new Label());
        setField(controller, "attemptsLabel", new Label());
        setField(controller, "streakLabel", new Label());
        setField(controller, "highStreakLabel", new Label());
        setField(controller, "hangmanImageView", new ImageView());
        setField(controller, "keyboardGrid", new GridPane());
        setField(controller, "tryAgainButton", new Button());

        Method handleLoss = GameController.class.getDeclaredMethod("handleLoss");
        handleLoss.setAccessible(true);
        handleLoss.invoke(controller);

        Label wordLabel = (Label) getField(controller, "wordLabel");
        assertEquals("C O N C I L I A", wordLabel.getText());
    }

    @Test
    void insufficientDiamondsAlertShouldExplainTheRequirement() throws Exception {
        GameController controller = new GameController();
        Method createAlert = GameController.class.getDeclaredMethod("createInsufficientDiamondsAlert");
        createAlert.setAccessible(true);

        Alert alert = (Alert) createAlert.invoke(controller);

        assertEquals(Alert.AlertType.WARNING, alert.getAlertType());
        assertEquals("Insufficient Diamonds", alert.getTitle());
        assertTrue(alert.getContentText().contains("10 diamonds"));
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Object getField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }
}
