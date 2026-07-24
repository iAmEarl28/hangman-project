package fr.quentincillierre.hangman.model;

public enum Category {

    ANIMALS("Animals"),
    COUNTRIES("Countries"),
    FOODS("Foods"),
    SPORTS("Sports"),
    TECHNOLOGY("Technology"),
    MOVIES("Movies"),
    SCIENCE("Science"),
    SPACE("Space"),
    JOBS("Jobs"),
    MUSIC("Music");

    private final String csvValue;

    Category(String csvValue) {
        this.csvValue = csvValue;
    }

    public String getCsvValue() {
        return csvValue;
    }

    public static Category fromString(String text) {

        for (Category category : values()) {

            if (category.csvValue.equalsIgnoreCase(text)) {
                return category;
            }

        }

        throw new IllegalArgumentException("Unknown category: " + text);

    }

}