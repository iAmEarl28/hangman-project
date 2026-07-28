package fr.quentincillierre.hangman.util;

import java.io.IOException;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import javafx.scene.layout.StackPane;

public class SceneManager {

    // Every screen is designed at this size. Instead of reflowing every
    // fixed/absolute layout in the FXML files, we scale the whole design
    // uniformly (preserving aspect ratio) to fit whatever size the window
    // currently is, so the app stays responsive on any screen/window size.
    private static final double DESIGN_WIDTH = 850;
    private static final double DESIGN_HEIGHT = 650;

    private static Stage stage;

    // Called once when the application starts
    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    // Switch to another FXML page, keeping the window's current size.
    public static void switchScene(String fxmlFile) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneManager.class.getResource("/application/" + fxmlFile)
            );

            Parent root = loader.load();

            double width = (stage.getScene() != null) ? stage.getScene().getWidth() : DESIGN_WIDTH;
            double height = (stage.getScene() != null) ? stage.getScene().getHeight() : DESIGN_HEIGHT;

            stage.setScene(createResponsiveScene(root, width, height));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Wraps a loaded screen so it scales (and stays centered) as the window
     * is resized, instead of clipping or leaving the layout fixed.
     */
    public static Scene createResponsiveScene(Parent root) {
        return createResponsiveScene(root, DESIGN_WIDTH, DESIGN_HEIGHT);
    }

    private static Scene createResponsiveScene(Parent root, double initialWidth, double initialHeight) {
        // Play background music right from the very start
        SoundManager.getInstance().playBackgroundMusic();

        // Automatically play clicked.mp3 whenever ANY button in the app is clicked
        root.addEventFilter(ActionEvent.ACTION, event -> {
            if (event.getTarget() instanceof Button) {
                SoundManager.getInstance().playClickSound();
            }
        });

        StackPane container = new StackPane();
        container.getStyleClass().add("root-bg");

        DoubleBinding scale = Bindings.createDoubleBinding(
                () -> Math.max(0.1, Math.min(container.getWidth() / DESIGN_WIDTH, container.getHeight() / DESIGN_HEIGHT)),
                container.widthProperty(), container.heightProperty());

        root.scaleXProperty().bind(scale);
        root.scaleYProperty().bind(scale);

        container.getChildren().add(root);

        Scene scene = new Scene(container, initialWidth, initialHeight);
        scene.getStylesheets().add(SceneManager.class.getResource("/application/styles.css").toExternalForm());
        return scene;
    }
}