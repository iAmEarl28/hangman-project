package fr.quentincillierre.hangman.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central place that knows which "resources/pictures" folder belongs to
 * each selectable character, and how to build the classpath paths for:
 * - the in-progress hangman stage pictures (0-hangman.png ... 10-hangman.png)
 * - the winning pose picture (winningpose.png)
 * - the defeat picture (10-hangman.png, the last picture in the folder)
 *
 * Also stores per-character shop prices, win-bonus diamond amounts, character names,
 * trailer definitions, and sequential order.
 */
public final class CharacterManager {

    // Ordered list of character keys for sequential unlocking
    private static final String[] CHARACTER_ORDER = {"boy1", "girl1", "boy2", "girl2", "robot"};

    // Key used by CharacterController (and stored on PlayerData) -> folder name
    // under src/main/resources/pictures/
    private static final Map<String, String> CHARACTER_FOLDERS = buildCharacterFolders();

    // Key -> small full-color portrait file under
    // src/main/resources/application/characters_pg3/ (the artwork already
    // used on the character-selection screen), used for the persistent
    // portrait shown on the game screen.
    private static final Map<String, String> CHARACTER_PORTRAITS = buildCharacterPortraits();

    // Diamond price required to unlock each character (0 = always free).
    private static final Map<String, Integer> CHARACTER_PRICES = buildCharacterPrices();

    // Bonus diamonds awarded when the player WINS a round with this character.
    private static final Map<String, Integer> CHARACTER_BONUS = buildCharacterBonus();

    // Character display names
    private static final Map<String, String> CHARACTER_NAMES = buildCharacterNames();

    // Short trailer definitions / lore for each character
    private static final Map<String, String> CHARACTER_DEFINITIONS = buildCharacterDefinitions();

    // Used whenever no character has been selected yet (defensive default,
    // should not normally happen since a character must be picked before
    // reaching the game screen).
    private static final String DEFAULT_CHARACTER_KEY = "boy1";

    // Last stage index -> matches the "11 pictures" (0-hangman.png .. 10-hangman.png)
    private static final int DEFEAT_STAGE_INDEX = 10;

    private static Map<String, String> buildCharacterFolders() {
        Map<String, String> folders = new LinkedHashMap<>();
        folders.put("boy1", "character1_boy1");
        folders.put("girl1", "character2_girl2");
        folders.put("boy2", "character3_boy2");
        folders.put("girl2", "character4_girl2");
        folders.put("robot", "character5_boy3");
        return Collections.unmodifiableMap(folders);
    }

    private static Map<String, String> buildCharacterPortraits() {
        Map<String, String> portraits = new LinkedHashMap<>();
        portraits.put("boy1", "boy1.png");
        portraits.put("girl1", "girl1.png");
        portraits.put("boy2", "boy2.png");
        portraits.put("girl2", "girl2.png");
        portraits.put("robot", "boy3.png");
        return Collections.unmodifiableMap(portraits);
    }

    private static Map<String, Integer> buildCharacterPrices() {
        Map<String, Integer> prices = new LinkedHashMap<>();
        prices.put("boy1",   0);   // JOEY-kun   – free
        prices.put("girl1",  60);  // LON-kun    – 60 diamonds
        prices.put("boy2",   70);  // JOBOY-kun  – 70 diamonds
        prices.put("girl2",  80);  // KAREN-kun  – 80 diamonds
        prices.put("robot",  100); // JOHNNY-kun – 100 diamonds
        return Collections.unmodifiableMap(prices);
    }

    private static Map<String, Integer> buildCharacterBonus() {
        Map<String, Integer> bonus = new LinkedHashMap<>();
        bonus.put("boy1",   0); // JOEY-kun   – no bonus
        bonus.put("girl1",  1); // LON-kun    – +1 diamond on win
        bonus.put("boy2",   2); // JOBOY-kun  – +2 diamonds on win
        bonus.put("girl2",  3); // KAREN-kun  – +3 diamonds on win
        bonus.put("robot",  5); // JOHNNY-kun – +5 diamonds on win
        return Collections.unmodifiableMap(bonus);
    }

    private static Map<String, String> buildCharacterNames() {
        Map<String, String> names = new LinkedHashMap<>();
        names.put("boy1",  "JOEY-kun");
        names.put("girl1", "LON-kun");
        names.put("boy2",  "JOBOY-kun");
        names.put("girl2", "KAREN-kun");
        names.put("robot", "JOHNNY-kun");
        return Collections.unmodifiableMap(names);
    }

    private static Map<String, String> buildCharacterDefinitions() {
        Map<String, String> defs = new LinkedHashMap<>();
        defs.put("boy1",  "The cheerful beginner cutie who loves solving word puzzles!");
        defs.put("girl1", "A quiet scholar who rewards you with +1 extra diamond when saved.");
        defs.put("boy2",  "An energetic adventurer who gives you +2 extra diamonds when saved.");
        defs.put("girl2", "A sharp strategist who grants +3 extra diamonds when saved.");
        defs.put("robot", "The legendary hero who bestows maximum +5 extra diamonds when saved!");
        return Collections.unmodifiableMap(defs);
    }

    private CharacterManager() {
    }

    public static String[] getCharacterOrder() {
        return CHARACTER_ORDER.clone();
    }

    /**
     * Returns the key of the character that must be unlocked prior to the given key,
     * or null if the given key is the first character (boy1).
     */
    public static String getPreviousCharacterKey(String characterKey) {
        for (int i = 0; i < CHARACTER_ORDER.length; i++) {
            if (CHARACTER_ORDER[i].equals(characterKey)) {
                return (i > 0) ? CHARACTER_ORDER[i - 1] : null;
            }
        }
        return null;
    }

    /**
     * @return display name of the given character (e.g. "JOEY-kun").
     */
    public static String getName(String characterKey) {
        return CHARACTER_NAMES.getOrDefault(characterKey, "Cutie");
    }

    /**
     * @return short trailer definition / lore for the given character.
     */
    public static String getDefinition(String characterKey) {
        return CHARACTER_DEFINITIONS.getOrDefault(characterKey, "");
    }

    /**
     * @return the picture folder name for the given character key, falling
     * back to the default character if the key is null/unknown.
     */
    public static String getFolderName(String characterKey) {
        String resolvedKey = (characterKey != null && CHARACTER_FOLDERS.containsKey(characterKey))
                ? characterKey
                : DEFAULT_CHARACTER_KEY;
        return CHARACTER_FOLDERS.get(resolvedKey);
    }

    /**
     * @return the diamond price required to unlock the given character (0 if free/unknown).
     */
    public static int getPrice(String characterKey) {
        return CHARACTER_PRICES.getOrDefault(characterKey, 0);
    }

    /**
     * @return bonus diamonds awarded when winning a round with the given character.
     */
    public static int getBonusDiamonds(String characterKey) {
        return CHARACTER_BONUS.getOrDefault(characterKey, 0);
    }

    /**
     * Classpath path to a given hangman stage picture (0-hangman.png ... 10-hangman.png)
     * for the selected character.
     */
    public static String resolveStageImagePath(String characterKey, int wrongCount) {
        return "/pictures/" + getFolderName(characterKey) + "/" + wrongCount + "-hangman.png";
    }

    /**
     * Classpath path to the selected character's winning pose picture.
     */
    public static String resolveWinningPoseImagePath(String characterKey) {
        return "/pictures/" + getFolderName(characterKey) + "/winningpose.png";
    }

    /**
     * Classpath path to the selected character's defeat picture (the 11th /
     * last picture in the folder, i.e. 10-hangman.png).
     */
    public static String resolveDefeatImagePath(String characterKey) {
        return resolveStageImagePath(characterKey, DEFEAT_STAGE_INDEX);
    }

    /**
     * Classpath path to the small full-color portrait used to keep the
     * selected character visible on the game screen (persistent portrait).
     */
    public static String resolvePortraitImagePath(String characterKey) {
        String resolvedKey = (characterKey != null && CHARACTER_PORTRAITS.containsKey(characterKey))
                ? characterKey
                : DEFAULT_CHARACTER_KEY;
        return "/application/characters_pg3/" + CHARACTER_PORTRAITS.get(resolvedKey);
    }
}
