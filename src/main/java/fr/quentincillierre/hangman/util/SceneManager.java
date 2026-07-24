package fr.quentincillierre.hangman.util;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private static Stage stage;

    // Called once when the application starts
    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    // Switch to another FXML page
    public static void switchScene(String fxmlFile) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneManager.class.getResource("/application/" + fxmlFile)
            );

            Parent root = loader.load();

            stage.setScene(new Scene(root));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}