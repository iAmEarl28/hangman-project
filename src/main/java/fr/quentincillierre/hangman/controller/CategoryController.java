package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.model.Category;
import fr.quentincillierre.hangman.util.GameSession;
import fr.quentincillierre.hangman.util.SceneManager;

public class CategoryController {

    public void animalsClicked() {

        GameSession.getPlayerData().setCategory(Category.ANIMALS);

        SceneManager.switchScene("character-view.fxml");

    }

    public void countriesClicked() {

        GameSession.getPlayerData().setCategory(Category.COUNTRIES);

        SceneManager.switchScene("character-view.fxml");

    }

    public void foodsClicked() {

        GameSession.getPlayerData().setCategory(Category.FOODS);

        SceneManager.switchScene("character-view.fxml");

    }

    public void sportsClicked() {

        GameSession.getPlayerData().setCategory(Category.SPORTS);

        SceneManager.switchScene("character-view.fxml");

    }

    public void technologyClicked() {

        GameSession.getPlayerData().setCategory(Category.TECHNOLOGY);

        SceneManager.switchScene("character-view.fxml");

    }

    public void moviesClicked() {

        GameSession.getPlayerData().setCategory(Category.MOVIES);

        SceneManager.switchScene("character-view.fxml");

    }

    public void scienceClicked() {

        GameSession.getPlayerData().setCategory(Category.SCIENCE);

        SceneManager.switchScene("character-view.fxml");

    }

    public void spaceClicked() {

        GameSession.getPlayerData().setCategory(Category.SPACE);

        SceneManager.switchScene("character-view.fxml");

    }

    public void jobsClicked() {

        GameSession.getPlayerData().setCategory(Category.JOBS);

        SceneManager.switchScene("character-view.fxml");

    }

    public void musicClicked() {

        GameSession.getPlayerData().setCategory(Category.MUSIC);

        SceneManager.switchScene("character-view.fxml");

    }

}