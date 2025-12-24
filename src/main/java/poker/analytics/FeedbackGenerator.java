package poker.analytics;

import poker.model.*;

import java.util.ArrayList;
import java.util.List;

public class FeedbackGenerator {

    public String generateContextualFeedback(HandAnalysis analysis) {
        StringBuilder feedback = new StringBuilder();
        HandStrength strength = getHandStrength(analysis.holeCards());

        if (strength == HandStrength.TRASH &&
            analysis.playerAction().getType() != ActionType.FOLD) {
            feedback.append("\n[Правило #1: Играй тайтово]\n");
            feedback.append("Рука ").append(analysis.holeCards().getNotation());
            feedback.append(" - мусор. Новички играют слишком много рук.\n");
            feedback.append("Терпение = экономия фишек!\n");
        }

        if (isLikelyBluff(analysis) && !analysis.wasOptimal()) {
            feedback.append("\n[Правило #3: Не блефуй против новичков]\n");
            feedback.append("Calling station'ы коллируют слишком часто.\n");
            feedback.append("Блеф против них обычно минус.\n");
        }

        if (analysis.stage().isPostflop() && analysis.wasOptimal() &&
            analysis.recommendation() == ActionRecommendation.RAISE) {
            feedback.append("\n[Правило #2: Вэлью-бет толще]\n");
            feedback.append("Правильно ставишь с сильной рукой!\n");
            feedback.append("Новички заплатят.\n");
        }

        return feedback.toString();
    }

    public List<String> getEndOfHandTips(SessionStats stats, HandAnalysis lastHand) {
        List<String> tips = new ArrayList<>();

        if (stats.getOptimalPlayRate() < 0.5 && stats.getTotalDecisions() > 5) {
            tips.add("Много неоптимальных решений. Попробуй нажимать [H] для подсказок.");
        }

        if (!lastHand.wasOptimal()) {
            tips.add("В этой руке лучше было: " + lastHand.recommendation().getRussianName());
        }

        return tips;
    }

    public String getPositionReminder(Position position) {
        if (position.isEarly()) {
            return "Ранняя позиция - играй только TOP руки (AA, KK, QQ, AK)";
        }
        if (position.isMiddle()) {
            return "Средняя позиция - можно добавить JJ, TT, AQ";
        }
        if (position.isLate()) {
            return "Поздняя позиция - можно играть шире, у тебя информационное преимущество";
        }
        return "";
    }

    public String getHandStrengthExplanation(HoleCards cards) {
        HandStrength strength = getHandStrength(cards);
        String notation = cards.getNotation();

        return switch (strength) {
            case PREMIUM -> notation + " - ПРЕМИУМ!\n" +
                "Рейзь всегда, ререйзь против рейза.\n" +
                "Это топ-5 рук в покере.";

            case STRONG -> notation + " - Сильная рука.\n" +
                "Рейзь с любой позиции.\n" +
                "Коллируй одиночный рейз, фолди против нескольких.";

            case PLAYABLE -> notation + " - Играбельная.\n" +
                "Рейзь с поздней позиции (CO, BTN).\n" +
                "С ранней - лучше фолд.";

            case TRASH -> notation + " - Мусор!\n" +
                "Фолд с любой позиции.\n" +
                "Терпение = прибыль.";
        };
    }

    public String getOddsExplanation(OddsResult odds, GameStage stage) {
        StringBuilder sb = new StringBuilder();

        if (odds.outs() > 0) {
            sb.append("У тебя ").append(odds.outs()).append(" аутов.\n");

            if (stage == GameStage.FLOP) {
                sb.append("Правило 4-2: ").append(odds.outs()).append(" × 4 = ");
                sb.append((int)(odds.equity() * 100)).append("% шанс к риверу.\n");
            } else if (stage == GameStage.TURN) {
                sb.append("Правило 4-2: ").append(odds.outs()).append(" × 2 = ");
                sb.append((int)(odds.equity() * 100)).append("% шанс на ривере.\n");
            }
        }

        if (odds.potOdds() > 0) {
            sb.append("Пот-оддсы: ").append(String.format("%.1f%%", odds.potOdds() * 100)).append("\n");

            if (odds.shouldCall()) {
                sb.append("Твоё эквити БОЛЬШЕ пот-оддсов → КОЛЛ выгоден!\n");
            } else {
                sb.append("Твоё эквити МЕНЬШЕ пот-оддсов → ФОЛД лучше.\n");
            }
        }

        return sb.toString();
    }

    private HandStrength getHandStrength(HoleCards cards) {
        String notation = cards.getNotation();

        if (notation.matches("AA|KK|QQ|AKs|AKo")) {
            return HandStrength.PREMIUM;
        }
        if (notation.matches("JJ|TT|AQs|AQo|AJs|KQs")) {
            return HandStrength.STRONG;
        }
        if (notation.matches("99|88|77|66|55|44|33|22|" +
            "A[T98765432]s|K[JT]s|Q[JT]s|JTs|T9s|98s|87s|76s|65s")) {
            return HandStrength.PLAYABLE;
        }
        return HandStrength.TRASH;
    }

    private boolean isLikelyBluff(HandAnalysis analysis) {
        if (analysis.playerAction().isAggressive() &&
            analysis.oddsResult() != null &&
            analysis.oddsResult().outs() == 0) {
            return true;
        }
        return false;
    }
}
