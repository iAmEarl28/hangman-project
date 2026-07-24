package fr.quentincillierre.hangman.application;

import fr.quentincillierre.hangman.util.SceneManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

   @Override
public void start(Stage stage) throws Exception {

    SceneManager.setStage(stage);

    FXMLLoader loader =
            new FXMLLoader(getClass().getResource("/application/start-view.fxml"));

    Scene scene = new Scene(loader.load());

    stage.setTitle("Hangman");
    stage.setScene(scene);
    stage.setResizable(false);
    stage.show();
}

    public static void main(String[] args) {
        launch(args);
    }

}