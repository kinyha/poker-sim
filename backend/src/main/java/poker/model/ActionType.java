package poker.model;

public enum ActionType {
    FOLD("Fold", "Фолд"),
    CHECK("Check", "Чек"),
    CALL("Call", "Колл"),
    BET("Bet", "Бет"),
    RAISE("Raise", "Рейз"),
    ALL_IN("All-In", "Олл-ин");

    private final String displayName;
    private final String russianName;

    ActionType(String displayName, String russianName) {
        this.displayName = displayName;
        this.russianName = russianName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRussianName() {
        return russianName;
    }
}
