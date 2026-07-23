package fr.quentincillierre.hangman.application; // tells Java that this class belongs inside the application folder.
// imports - simply means using another class that was already made
import java.io.IOException; 

import fr.quentincillierre.hangman.controller.GameController; // to access GameController class
import javafx.application.Application; // application is not inside your project, it is part of the JavaFX library
import javafx.fxml.FXMLLoader; // without this java cannot build your window.
import javafx.scene.Scene; // a scene is the entire window content.
import javafx.stage.Stage; // a stage is the window itself.

public class MainApp extends Application {

    @Override // replacing a method that already exists"
    public void start(Stage stage) throws IOException { // application class already has a method called start(), you are writing your own version.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/quentincillierre/hangman/application/game-view.fxml")); // means create an FXML loader and tell it where the UI file is.
        Scene scene = new Scene(loader.load(), 850, 650); // loader read the FXML, the numbers are simply the height and width"
        GameController controller = loader.getController(); // when the loader reads the FXML, it automatically creates a GameController object. This line asks:"Give me the controller you created."Now the controller variable points to that object.

        scene.setOnKeyTyped(event -> {
            controller.handleKeyboardInput(event.getCharacter());
        }); // tells the GameController if the user type in the keyboard.

        stage.setTitle("Hangman Game"); // name of the stage - you can see it in the uppermost area of the window
        stage.setScene(scene); // place the scene inside the window
        stage.setResizable(false); // to do not let the user resize the window
        stage.show(); // to make the windows appear
    } // public means everyone can use this class.

    public static void main(String[] args) {
        launch(args);
    }
}