package fr.quentincillierre.hangman.application;

import fr.quentincillierre.hangman.util.SceneManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    // Smallest size the window can be shrunk to before content would become
    // unreadably small; the responsive scaling in SceneManager handles
    // everything above this.
    private static final double MIN_WIDTH = 480;
    private static final double MIN_HEIGHT = 370;

    @Override
    public void start(Stage stage) throws Exception {

        SceneManager.setStage(stage);

        FXMLLoader loader =
                new FXMLLoader(getClass().getResource("/application/start-view.fxml"));

        Parent root = loader.load();
        Scene scene = SceneManager.createResponsiveScene(root);

        for (int size : new int[] {256, 128, 64, 32}) {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/application/app-icon-" + size + ".png")));
        }
        stage.setTitle("Hangman Cutie");
        stage.setScene(scene);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}