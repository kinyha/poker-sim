package poker.web.dto;

import poker.model.Card;

public record CardDto(
    String rank,
    String suit,
    String display
) {
    public static CardDto from(Card card) {
        return new CardDto(
            card.getRank().name(),
            card.getSuit().name(),
            card.toString()
        );
    }
}
