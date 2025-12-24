package poker.model;

public enum HandRank {
    HIGH_CARD(1, "High Card", "Старшая карта"),
    PAIR(2, "Pair", "Пара"),
    TWO_PAIR(3, "Two Pair", "Две пары"),
    THREE_OF_A_KIND(4, "Three of a Kind", "Тройка"),
    STRAIGHT(5, "Straight", "Стрит"),
    FLUSH(6, "Flush", "Флеш"),
    FULL_HOUSE(7, "Full House", "Фулл-хаус"),
    FOUR_OF_A_KIND(8, "Four of a Kind", "Каре"),
    STRAIGHT_FLUSH(9, "Straight Flush", "Стрит-флеш"),
    ROYAL_FLUSH(10, "Royal Flush", "Роял-флеш");

    private final int strength;
    private final String displayName;
    private final String russianName;

    HandRank(int strength, String displayName, String russianName) {
        this.strength = strength;
        this.displayName = displayName;
        this.russianName = russianName;
    }

    public int getStrength() {
        return strength;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRussianName() {
        return russianName;
    }
}
