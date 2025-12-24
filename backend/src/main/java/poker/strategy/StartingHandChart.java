package poker.strategy;

import poker.model.*;

import java.util.*;

public class StartingHandChart {

    private static final Set<String> PREMIUM = Set.of(
        "AA", "KK", "QQ", "AKs", "AKo"
    );

    private static final Set<String> STRONG = Set.of(
        "JJ", "TT", "AQs", "AQo", "AJs", "KQs"
    );

    private static final Set<String> PLAYABLE = Set.of(
        "99", "88", "77", "66", "55", "44", "33", "22",
        "ATs", "A9s", "A8s", "A7s", "A6s", "A5s", "A4s", "A3s", "A2s",
        "KJs", "KTs", "QJs", "QTs", "JTs", "T9s", "98s", "87s", "76s", "65s"
    );

    private static final Set<String> MARGINAL = Set.of(
        "ATo", "A9o", "KJo", "KTo", "QJo", "JTo", "T9o", "98o"
    );

    public HandStrength getHandStrength(HoleCards cards) {
        String notation = cards.getNotation();

        if (PREMIUM.contains(notation)) {
            return HandStrength.PREMIUM;
        }
        if (STRONG.contains(notation)) {
            return HandStrength.STRONG;
        }
        if (PLAYABLE.contains(notation)) {
            return HandStrength.PLAYABLE;
        }
        return HandStrength.TRASH;
    }

    public ActionRecommendation getRecommendation(HoleCards cards, Position position,
                                                   boolean facingRaise, int raiserCount) {
        HandStrength strength = getHandStrength(cards);

        if (strength == HandStrength.PREMIUM) {
            if (facingRaise) {
                return ActionRecommendation.RERAISE;
            }
            return ActionRecommendation.RAISE;
        }

        if (strength == HandStrength.STRONG) {
            if (facingRaise && raiserCount > 1) {
                return ActionRecommendation.FOLD;
            }
            if (facingRaise) {
                return ActionRecommendation.CALL;
            }
            return ActionRecommendation.RAISE;
        }

        if (strength == HandStrength.PLAYABLE) {
            if (facingRaise && raiserCount > 1) {
                return ActionRecommendation.FOLD;
            }
            if (facingRaise) {
                if (position.isLate()) {
                    return ActionRecommendation.CALL;
                }
                return ActionRecommendation.FOLD;
            }
            if (position.isLate()) {
                return ActionRecommendation.RAISE;
            }
            if (position.isMiddle()) {
                return ActionRecommendation.CALL;
            }
            return ActionRecommendation.FOLD;
        }

        return ActionRecommendation.FOLD;
    }

    public String getHandDescription(HoleCards cards) {
        HandStrength strength = getHandStrength(cards);
        String notation = cards.getNotation();

        return switch (strength) {
            case PREMIUM -> notation + " - ПРЕМИУМ! Рейзить всегда.";
            case STRONG -> notation + " - Сильная рука. Рейзить с позиции.";
            case PLAYABLE -> notation + " - Играбельная. Колл/рейз с поздней позиции.";
            case TRASH -> notation + " - Мусор. Фолд!";
        };
    }

    public String getPositionAdvice(Position position) {
        if (position.isEarly()) {
            return "Ранняя позиция (UTG/UTG+1): играй только премиум руки (AA, KK, QQ, AK)";
        }
        if (position.isMiddle()) {
            return "Средняя позиция: можно добавить JJ, TT, AQ";
        }
        if (position.isLate()) {
            return "Поздняя позиция: можно играть шире, много информации от других";
        }
        if (position.isBlind()) {
            return "Блайнд: уже вложены деньги, можно защищать шире";
        }
        return "";
    }

    public boolean shouldOpenRaise(HoleCards cards, Position position) {
        HandStrength strength = getHandStrength(cards);

        return switch (strength) {
            case PREMIUM -> true;
            case STRONG -> true;
            case PLAYABLE -> position.isMiddle() || position.isLate();
            case TRASH -> false;
        };
    }

    public boolean shouldCallRaise(HoleCards cards, Position position, int raiseSize, int bigBlind) {
        HandStrength strength = getHandStrength(cards);
        double raiseBBs = (double) raiseSize / bigBlind;

        if (strength == HandStrength.PREMIUM) {
            return true;
        }

        if (strength == HandStrength.STRONG) {
            return raiseBBs <= 5;
        }

        if (strength == HandStrength.PLAYABLE) {
            return position.isLate() && raiseBBs <= 3;
        }

        return false;
    }

    public int getSuggestedRaiseSize(int bigBlind, int pot, boolean hasLimpers) {
        if (hasLimpers) {
            return bigBlind * 4;
        }
        return bigBlind * 3;
    }
}
