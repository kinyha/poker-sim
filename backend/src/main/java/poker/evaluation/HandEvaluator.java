package poker.evaluation;

import poker.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class HandEvaluator {

    public Hand evaluate(List<Card> allCards) {
        if (allCards.size() < 5) {
            throw new IllegalArgumentException("Need at least 5 cards to evaluate");
        }

        if (allCards.size() == 5) {
            return evaluateFiveCards(allCards);
        }

        List<List<Card>> combinations = generateCombinations(allCards, 5);
        Hand best = null;

        for (List<Card> combo : combinations) {
            Hand hand = evaluateFiveCards(combo);
            if (best == null || hand.compareTo(best) > 0) {
                best = hand;
            }
        }

        return best;
    }

    private Hand evaluateFiveCards(List<Card> cards) {
        List<Card> sorted = new ArrayList<>(cards);
        sorted.sort((a, b) -> Integer.compare(b.getRank().getValue(), a.getRank().getValue()));

        if (isRoyalFlush(sorted)) {
            return makeHand(HandRank.ROYAL_FLUSH, sorted, List.of(14));
        }
        if (isStraightFlush(sorted)) {
            return makeStraightHand(HandRank.STRAIGHT_FLUSH, sorted);
        }
        if (isFourOfAKind(sorted)) {
            return makeFourOfAKind(sorted);
        }
        if (isFullHouse(sorted)) {
            return makeFullHouse(sorted);
        }
        if (isFlush(sorted)) {
            return makeFlush(sorted);
        }
        if (isStraight(sorted)) {
            return makeStraightHand(HandRank.STRAIGHT, sorted);
        }
        if (isThreeOfAKind(sorted)) {
            return makeThreeOfAKind(sorted);
        }
        if (isTwoPair(sorted)) {
            return makeTwoPair(sorted);
        }
        if (isPair(sorted)) {
            return makePair(sorted);
        }
        return makeHighCard(sorted);
    }

    private boolean isFlush(List<Card> cards) {
        Suit first = cards.get(0).getSuit();
        return cards.stream().allMatch(c -> c.getSuit() == first);
    }

    private boolean isStraight(List<Card> cards) {
        List<Integer> values = cards.stream()
            .map(c -> c.getRank().getValue())
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());

        if (isConsecutive(values)) {
            return true;
        }

        if (values.equals(List.of(14, 5, 4, 3, 2))) {
            return true;
        }

        return false;
    }

    private boolean isConsecutive(List<Integer> values) {
        for (int i = 0; i < values.size() - 1; i++) {
            if (values.get(i) - values.get(i + 1) != 1) {
                return false;
            }
        }
        return true;
    }

    private boolean isRoyalFlush(List<Card> cards) {
        if (!isFlush(cards)) return false;
        List<Integer> values = cards.stream()
            .map(c -> c.getRank().getValue())
            .sorted(Comparator.reverseOrder())
            .toList();
        return values.equals(List.of(14, 13, 12, 11, 10));
    }

    private boolean isStraightFlush(List<Card> cards) {
        return isFlush(cards) && isStraight(cards);
    }

    private Map<Integer, Long> getRankCounts(List<Card> cards) {
        return cards.stream()
            .collect(Collectors.groupingBy(
                c -> c.getRank().getValue(),
                Collectors.counting()
            ));
    }

    private boolean isFourOfAKind(List<Card> cards) {
        return getRankCounts(cards).containsValue(4L);
    }

    private boolean isFullHouse(List<Card> cards) {
        Map<Integer, Long> counts = getRankCounts(cards);
        return counts.containsValue(3L) && counts.containsValue(2L);
    }

    private boolean isThreeOfAKind(List<Card> cards) {
        Map<Integer, Long> counts = getRankCounts(cards);
        return counts.containsValue(3L) && !counts.containsValue(2L);
    }

    private boolean isTwoPair(List<Card> cards) {
        Map<Integer, Long> counts = getRankCounts(cards);
        return counts.values().stream().filter(v -> v == 2).count() == 2;
    }

    private boolean isPair(List<Card> cards) {
        Map<Integer, Long> counts = getRankCounts(cards);
        return counts.values().stream().filter(v -> v == 2).count() == 1 &&
               !counts.containsValue(3L);
    }

    private Hand makeHand(HandRank rank, List<Card> cards, List<Integer> kickers) {
        return new Hand(rank, cards, kickers);
    }

    private Hand makeStraightHand(HandRank rank, List<Card> cards) {
        List<Integer> values = cards.stream()
            .map(c -> c.getRank().getValue())
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());

        if (values.equals(List.of(14, 5, 4, 3, 2))) {
            return makeHand(rank, cards, List.of(5));
        }

        return makeHand(rank, cards, List.of(values.get(0)));
    }

    private Hand makeFourOfAKind(List<Card> cards) {
        Map<Integer, Long> counts = getRankCounts(cards);
        int quadRank = counts.entrySet().stream()
            .filter(e -> e.getValue() == 4)
            .mapToInt(Map.Entry::getKey)
            .findFirst().orElseThrow();
        int kicker = counts.entrySet().stream()
            .filter(e -> e.getValue() == 1)
            .mapToInt(Map.Entry::getKey)
            .findFirst().orElseThrow();

        return makeHand(HandRank.FOUR_OF_A_KIND, cards, List.of(quadRank, kicker));
    }

    private Hand makeFullHouse(List<Card> cards) {
        Map<Integer, Long> counts = getRankCounts(cards);
        int tripsRank = counts.entrySet().stream()
            .filter(e -> e.getValue() == 3)
            .mapToInt(Map.Entry::getKey)
            .findFirst().orElseThrow();
        int pairRank = counts.entrySet().stream()
            .filter(e -> e.getValue() == 2)
            .mapToInt(Map.Entry::getKey)
            .findFirst().orElseThrow();

        return makeHand(HandRank.FULL_HOUSE, cards, List.of(tripsRank, pairRank));
    }

    private Hand makeFlush(List<Card> cards) {
        List<Integer> kickers = cards.stream()
            .map(c -> c.getRank().getValue())
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());
        return makeHand(HandRank.FLUSH, cards, kickers);
    }

    private Hand makeThreeOfAKind(List<Card> cards) {
        Map<Integer, Long> counts = getRankCounts(cards);
        int tripsRank = counts.entrySet().stream()
            .filter(e -> e.getValue() == 3)
            .mapToInt(Map.Entry::getKey)
            .findFirst().orElseThrow();
        List<Integer> kickers = new ArrayList<>();
        kickers.add(tripsRank);
        counts.entrySet().stream()
            .filter(e -> e.getValue() == 1)
            .map(Map.Entry::getKey)
            .sorted(Comparator.reverseOrder())
            .forEach(kickers::add);

        return makeHand(HandRank.THREE_OF_A_KIND, cards, kickers);
    }

    private Hand makeTwoPair(List<Card> cards) {
        Map<Integer, Long> counts = getRankCounts(cards);
        List<Integer> pairs = counts.entrySet().stream()
            .filter(e -> e.getValue() == 2)
            .map(Map.Entry::getKey)
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());
        int kicker = counts.entrySet().stream()
            .filter(e -> e.getValue() == 1)
            .mapToInt(Map.Entry::getKey)
            .findFirst().orElseThrow();

        List<Integer> kickers = new ArrayList<>();
        kickers.add(pairs.get(0));
        kickers.add(pairs.get(1));
        kickers.add(kicker);

        return makeHand(HandRank.TWO_PAIR, cards, kickers);
    }

    private Hand makePair(List<Card> cards) {
        Map<Integer, Long> counts = getRankCounts(cards);
        int pairRank = counts.entrySet().stream()
            .filter(e -> e.getValue() == 2)
            .mapToInt(Map.Entry::getKey)
            .findFirst().orElseThrow();
        List<Integer> kickers = new ArrayList<>();
        kickers.add(pairRank);
        counts.entrySet().stream()
            .filter(e -> e.getValue() == 1)
            .map(Map.Entry::getKey)
            .sorted(Comparator.reverseOrder())
            .forEach(kickers::add);

        return makeHand(HandRank.PAIR, cards, kickers);
    }

    private Hand makeHighCard(List<Card> cards) {
        List<Integer> kickers = cards.stream()
            .map(c -> c.getRank().getValue())
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());
        return makeHand(HandRank.HIGH_CARD, cards, kickers);
    }

    private List<List<Card>> generateCombinations(List<Card> cards, int k) {
        List<List<Card>> result = new ArrayList<>();
        generateCombinationsHelper(cards, k, 0, new ArrayList<>(), result);
        return result;
    }

    private void generateCombinationsHelper(List<Card> cards, int k, int start,
                                            List<Card> current, List<List<Card>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < cards.size(); i++) {
            current.add(cards.get(i));
            generateCombinationsHelper(cards, k, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    public boolean hasMadeHand(List<Card> cards) {
        if (cards.size() < 5) return false;
        Hand hand = evaluate(cards);
        return hand.getRank().getStrength() >= HandRank.PAIR.getStrength();
    }

    public boolean hasFlushDraw(List<Card> cards) {
        if (cards.size() < 4) return false;
        Map<Suit, Long> suitCounts = cards.stream()
            .collect(Collectors.groupingBy(Card::getSuit, Collectors.counting()));
        return suitCounts.values().stream().anyMatch(count -> count == 4);
    }

    public boolean hasStraightDraw(List<Card> cards) {
        if (cards.size() < 4) return false;

        Set<Integer> values = cards.stream()
            .map(c -> c.getRank().getValue())
            .collect(Collectors.toSet());

        if (values.contains(14)) {
            values.add(1);
        }

        for (int start = 1; start <= 10; start++) {
            int count = 0;
            for (int v = start; v < start + 5; v++) {
                if (values.contains(v)) count++;
            }
            if (count >= 4) return true;
        }
        return false;
    }
}
