package fr.quentincillierre.hangman.model;

public class Word {

    private final Category category;
    private final Difficulty difficulty;
    private final String word;
    private final String definition;

    public Word(Category category,
                Difficulty difficulty,
                String word,
                String definition) {

        this.category = category;
        this.difficulty = difficulty;
        this.word = word.toUpperCase();
        this.definition = definition;
    }

    public Category getCategory() {
        return category;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public String getWord() {
        return word;
    }

    public String getDefinition() {
        return definition;
    }

}