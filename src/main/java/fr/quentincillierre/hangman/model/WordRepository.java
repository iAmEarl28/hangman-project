package fr.quentincillierre.hangman.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class WordRepository {

    public String getRandomWord() {
        List<String> words = new ArrayList<>();

        // Load the words.txt file from the resources folder
        InputStream is = getClass().getResourceAsStream("/words.txt");

        try (Scanner scanner = new Scanner(new BufferedReader(new InputStreamReader(is)))) {
            while (scanner.hasNextLine()) {
                words.add(scanner.nextLine().trim().toUpperCase());
            }
        } catch (Exception e) {
            return "COMPUTER"; // Fallback word
        }

        Random random = new Random();
        return words.get(random.nextInt(words.size()));
    }
}