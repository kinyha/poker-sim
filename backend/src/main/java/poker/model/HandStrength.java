package poker.model;

public enum HandStrength {
    PREMIUM(4, "Premium", "Премиум - рейзить всегда"),
    STRONG(3, "Strong", "Сильные - рейзить с позиции"),
    PLAYABLE(2, "Playable", "Играбельные - колл/рейз с поздней"),
    TRASH(1, "Trash", "Мусор - фолд");

    private final int tier;
    private final String displayName;
    private final String russianDescription;

    HandStrength(int tier, String displayName, String russianDescription) {
        this.tier = tier;
        this.displayName = displayName;
        this.russianDescription = russianDescription;
    }

    public int getTier() {
        return tier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRussianDescription() {
        return russianDescription;
    }
}
