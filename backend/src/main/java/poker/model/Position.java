package poker.model;

import java.util.ArrayList;
import java.util.List;

public enum Position {
    SMALL_BLIND(0, "SB", "Малый блайнд", true),
    BIG_BLIND(1, "BB", "Большой блайнд", true),
    UNDER_THE_GUN(2, "UTG", "UTG (ранняя)", false),
    UTG_PLUS_1(3, "UTG+1", "UTG+1 (ранняя)", false),
    MIDDLE_POSITION_1(4, "MP1", "MP1 (средняя)", false),
    MIDDLE_POSITION_2(5, "MP2", "MP2 (средняя)", false),
    HIJACK(6, "HJ", "Hijack (поздняя)", false),
    CUTOFF(7, "CO", "Cutoff (поздняя)", false),
    BUTTON(8, "BTN", "Button (лучшая!)", false);

    private final int order;
    private final String abbreviation;
    private final String russianName;
    private final boolean isBlind;

    Position(int order, String abbreviation, String russianName, boolean isBlind) {
        this.order = order;
        this.abbreviation = abbreviation;
        this.russianName = russianName;
        this.isBlind = isBlind;
    }

    public int getOrder() {
        return order;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getRussianName() {
        return russianName;
    }

    public boolean isBlind() {
        return isBlind;
    }

    public boolean isEarly() {
        return order >= 2 && order <= 3;
    }

    public boolean isMiddle() {
        return order >= 4 && order <= 5;
    }

    public boolean isLate() {
        return order >= 6;
    }

    public static List<Position> getPositionsForPlayerCount(int playerCount) {
        if (playerCount < 2 || playerCount > 9) {
            throw new IllegalArgumentException("Player count must be 2-9");
        }

        List<Position> positions = new ArrayList<>();

        if (playerCount == 2) {
            positions.add(BUTTON);
            positions.add(BIG_BLIND);
        } else if (playerCount == 3) {
            positions.add(BUTTON);
            positions.add(SMALL_BLIND);
            positions.add(BIG_BLIND);
        } else {
            positions.add(SMALL_BLIND);
            positions.add(BIG_BLIND);

            int remaining = playerCount - 2;
            Position[] availablePositions = {UNDER_THE_GUN, UTG_PLUS_1, MIDDLE_POSITION_1,
                                              MIDDLE_POSITION_2, HIJACK, CUTOFF, BUTTON};

            int startIdx = Math.max(0, 7 - remaining);
            for (int i = startIdx; i < 7 && positions.size() < playerCount; i++) {
                positions.add(availablePositions[i]);
            }
        }

        return positions;
    }
}
