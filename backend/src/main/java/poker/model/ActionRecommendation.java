package poker.model;

public enum ActionRecommendation {
    FOLD("Fold", "Фолд"),
    CHECK("Check", "Чек"),
    CALL("Call", "Колл"),
    RAISE("Raise", "Рейз"),
    RERAISE("Re-raise", "Ререйз");

    private final String displayName;
    private final String russianName;

    ActionRecommendation(String displayName, String russianName) {
        this.displayName = displayName;
        this.russianName = russianName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRussianName() {
        return russianName;
    }

    public boolean matchesAction(Action action) {
        return switch (this) {
            case FOLD -> action.getType() == ActionType.FOLD;
            case CHECK -> action.getType() == ActionType.CHECK;
            case CALL -> action.getType() == ActionType.CALL || action.getType() == ActionType.CHECK;
            case RAISE, RERAISE -> action.getType() == ActionType.RAISE ||
                                   action.getType() == ActionType.BET ||
                                   action.getType() == ActionType.ALL_IN;
        };
    }
}
