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

                String word = parts[2].trim();

                String definition = parts[3].trim();

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

    public Word getRandomWord(Category category,
                              Difficulty difficulty) {

        List<Word> filteredWords = new ArrayList<>();

        for (Word word : words) {

            if (word.getCategory() == category &&
                    word.getDifficulty() == difficulty) {

                filteredWords.add(word);

            }

        }

        if (filteredWords.isEmpty()) {

            return null;

        }

        Random random = new Random();

        return filteredWords.get(
                random.nextInt(filteredWords.size())
        );

    }

}