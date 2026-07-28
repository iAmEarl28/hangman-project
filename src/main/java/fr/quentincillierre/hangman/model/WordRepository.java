package fr.quentincillierre.hangman.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordRepository {

    private final List<Word> words = new ArrayList<>();

    public WordRepository() {

        loadWords();

    }

    private void loadWords() {

        try {

            InputStream inputStream =
                    getClass().getResourceAsStream("/words.csv");

            if (inputStream == null) {

                throw new RuntimeException("words.csv not found!");

            }

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(inputStream));

            // Skip header
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split(",", 4);

                if (parts.length != 4) {
                    continue;
                }

                Category category =
                        Category.fromString(parts[0].trim());

                Difficulty difficulty =
                        Difficulty.valueOf(parts[1].trim().toUpperCase());

                String word = sanitizeWord(parts[2].trim());

                String definition = parts[3].trim();

                if (word.isEmpty()) {
                    continue;
                }

                words.add(
                        new Word(
                                category,
                                difficulty,
                                word,
                                definition
                        )
                );

            }

            reader.close();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    private int getMinLength(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 3;
            case MEDIUM -> 5;
            case HARD -> 7;
        };
    }

    private int getMaxLength(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 5;
            case MEDIUM -> 8;
            case HARD -> Integer.MAX_VALUE;
        };
    }

    private String sanitizeWord(String word) {
        return word.replaceAll("[^A-Za-z]", "").toUpperCase();
    }

    public Word getRandomWord(Category category, Difficulty difficulty) {
        int minLen = getMinLength(difficulty);
        int maxLen = getMaxLength(difficulty);

        // Pass 1: exact category + difficulty + length range
        List<Word> filteredWords = new ArrayList<>();
        for (Word word : words) {
            if (word.getCategory() != category || word.getDifficulty() != difficulty) continue;
            int len = word.getWord().length();
            if (len >= minLen && len <= maxLen) {
                filteredWords.add(word);
            }
        }

        // Pass 2: category + difficulty, ignore length
        if (filteredWords.isEmpty()) {
            for (Word word : words) {
                if (word.getCategory() == category && word.getDifficulty() == difficulty) {
                    filteredWords.add(word);
                }
            }
        }

        // Pass 3: difficulty + length range, ignore category
        if (filteredWords.isEmpty()) {
            for (Word word : words) {
                if (word.getDifficulty() != difficulty) continue;
                int len = word.getWord().length();
                if (len >= minLen && len <= maxLen) {
                    filteredWords.add(word);
                }
            }
        }

        // Pass 4: difficulty only — last resort
        if (filteredWords.isEmpty()) {
            for (Word word : words) {
                if (word.getDifficulty() == difficulty) {
                    filteredWords.add(word);
                }
            }
        }

        if (filteredWords.isEmpty()) {
            return null;
        }

        return filteredWords.get(new Random().nextInt(filteredWords.size()));
    }

}