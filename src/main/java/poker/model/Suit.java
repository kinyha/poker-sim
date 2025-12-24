package poker.model;

public enum Suit {
    HEARTS("h", "\u2665"),
    DIAMONDS("d", "\u2666"),
    CLUBS("c", "\u2663"),
    SPADES("s", "\u2660");

    private final String code;
    private final String symbol;

    Suit(String code, String symbol) {
        this.code = code;
        this.symbol = symbol;
    }

    public String getCode() {
        return code;
    }

    public String getSymbol() {
        return symbol;
    }

    public static Suit fromCode(String code) {
        for (Suit suit : values()) {
            if (suit.code.equalsIgnoreCase(code)) {
                return suit;
            }
        }
        throw new IllegalArgumentException("Unknown suit code: " + code);
    }
}
