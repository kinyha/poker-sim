package poker.model;

public enum GameStage {
    PREFLOP(0, 0, "Preflop", "Префлоп"),
    FLOP(3, 1, "Flop", "Флоп"),
    TURN(4, 2, "Turn", "Тёрн"),
    RIVER(5, 3, "River", "Ривер"),
    SHOWDOWN(5, 4, "Showdown", "Вскрытие");

    private final int communityCardCount;
    private final int roundNumber;
    private final String displayName;
    private final String russianName;

    GameStage(int communityCardCount, int roundNumber, String displayName, String russianName) {
        this.communityCardCount = communityCardCount;
        this.roundNumber = roundNumber;
        this.displayName = displayName;
        this.russianName = russianName;
    }

    public int getCommunityCardCount() {
        return communityCardCount;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRussianName() {
        return russianName;
    }

    public GameStage next() {
        if (this == SHOWDOWN) {
            return SHOWDOWN;
        }
        return values()[ordinal() + 1];
    }

    public boolean isPreflop() {
        return this == PREFLOP;
    }

    public boolean isPostflop() {
        return this != PREFLOP && this != SHOWDOWN;
    }
}
