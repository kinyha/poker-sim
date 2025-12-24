package poker.odds;

import poker.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class OutsCalculator {

    public int calculateOuts(Player player, GameState state) {
        List<Card> holeCards = player.getHoleCards().getCards();
        List<Card> communityCards = state.getCommunityCards();

        List<Card> allKnown = new ArrayList<>(holeCards);
        allKnown.addAll(communityCards);

        Set<Card> knownSet = new HashSet<>(allKnown);

        int flushOuts = countFlushOuts(allKnown, knownSet);
        int straightOuts = countStraightOuts(allKnown, knownSet);
        int pairOuts = countOvercardOuts(holeCards, communityCards, knownSet);

        int totalOuts = flushOuts + straightOuts + pairOuts;

        return Math.min(totalOuts, 52 - allKnown.size());
    }

    public DrawInfo analyzeDraws(Player player, GameState state) {
        List<Card> holeCards = player.getHoleCards().getCards();
        List<Card> communityCards = state.getCommunityCards();

        List<Card> allCards = new ArrayList<>(holeCards);
        allCards.addAll(communityCards);

        return new DrawInfo(
            hasFlushDraw(allCards),
            hasOpenEndedStraightDraw(allCards),
            hasGutshot(allCards),
            hasOvercards(holeCards, communityCards)
        );
    }

    private int countFlushOuts(List<Card> cards, Set<Card> known) {
        Map<Suit, Long> suitCounts = cards.stream()
            .collect(Collectors.groupingBy(Card::getSuit, Collectors.counting()));

        for (Map.Entry<Suit, Long> entry : suitCounts.entrySet()) {
            if (entry.getValue() == 4) {
                return 9;
            }
        }
        return 0;
    }

    private int countStraightOuts(List<Card> cards, Set<Card> known) {
        Set<Integer> values = cards.stream()
            .map(c -> c.getRank().getValue())
            .collect(Collectors.toSet());

        if (values.contains(14)) {
            values.add(1);
        }

        for (int start = 1; start <= 10; start++) {
            int count = 0;
            List<Integer> missing = new ArrayList<>();

            for (int v = start; v < start + 5; v++) {
                if (values.contains(v)) {
                    count++;
                } else {
                    missing.add(v);
                }
            }

            if (count == 4 && missing.size() == 1) {
                int missingValue = missing.get(0);
                if (missingValue == start || missingValue == start + 4) {
                    return 8;
                } else {
                    return 4;
                }
            }
        }
        return 0;
    }

    private int countOvercardOuts(List<Card> holeCards, List<Card> communityCards, Set<Card> known) {
        if (communityCards.isEmpty()) {
            return 0;
        }

        int highestBoard = communityCards.stream()
            .mapToInt(c -> c.getRank().getValue())
            .max()
            .orElse(0);

        int outs = 0;
        for (Card holeCard : holeCards) {
            if (holeCard.getRank().getValue() > highestBoard) {
                outs += 3;
            }
        }

        return Math.min(outs, 6);
    }

    private boolean hasFlushDraw(List<Card> cards) {
        return cards.stream()
            .collect(Collectors.groupingBy(Card::getSuit, Collectors.counting()))
            .values().stream()
            .anyMatch(count -> count == 4);
    }

    private boolean hasOpenEndedStraightDraw(List<Card> cards) {
        Set<Integer> values = cards.stream()
            .map(c -> c.getRank().getValue())
            .collect(Collectors.toSet());

        if (values.contains(14)) {
            values.add(1);
        }

        for (int start = 2; start <= 10; start++) {
            int count = 0;
            for (int v = start; v < start + 4; v++) {
                if (values.contains(v)) count++;
            }
            if (count == 4) {
                boolean hasLow = values.contains(start - 1);
                boolean hasHigh = values.contains(start + 4);
                if (!hasLow && !hasHigh) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasGutshot(List<Card> cards) {
        Set<Integer> values = cards.stream()
            .map(c -> c.getRank().getValue())
            .collect(Collectors.toSet());

        if (values.contains(14)) {
            values.add(1);
        }

        for (int start = 1; start <= 10; start++) {
            int count = 0;
            int missingInMiddle = 0;

            for (int v = start; v < start + 5; v++) {
                if (values.contains(v)) {
                    count++;
                } else if (v > start && v < start + 4) {
                    missingInMiddle++;
                }
            }

            if (count == 4 && missingInMiddle == 1) {
                return true;
            }
        }
        return false;
    }

    private boolean hasOvercards(List<Card> holeCards, List<Card> communityCards) {
        if (communityCards.isEmpty()) {
            return false;
        }

        int highestBoard = communityCards.stream()
            .mapToInt(c -> c.getRank().getValue())
            .max()
            .orElse(0);

        return holeCards.stream()
            .anyMatch(c -> c.getRank().getValue() > highestBoard);
    }

    public record DrawInfo(
        boolean hasFlushDraw,
        boolean hasOpenEndedStraightDraw,
        boolean hasGutshot,
        boolean hasOvercards
    ) {
        public String getDescription() {
            List<String> draws = new ArrayList<>();
            if (hasFlushDraw) draws.add("Флеш-дро (9 аутов)");
            if (hasOpenEndedStraightDraw) draws.add("Двусторонний стрит-дро (8 аутов)");
            if (hasGutshot) draws.add("Гатшот (4 аута)");
            if (hasOvercards) draws.add("Оверкарты");

            if (draws.isEmpty()) {
                return "Нет дро";
            }
            return String.join(", ", draws);
        }

        public boolean hasAnyDraw() {
            return hasFlushDraw || hasOpenEndedStraightDraw || hasGutshot;
        }
    }
}
