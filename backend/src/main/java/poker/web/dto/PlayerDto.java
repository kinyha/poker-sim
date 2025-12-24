package poker.web.dto;

import poker.model.Player;
import poker.model.Card;
import poker.model.Position;

import java.util.List;

public record PlayerDto(
    String name,
    int chips,
    int currentBet,
    Position position,
    boolean folded,
    boolean allIn,
    boolean isHuman,
    List<CardDto> holeCards
) {
    public static PlayerDto from(Player player, boolean showCards) {
        List<CardDto> cards = null;
        if (showCards && player.getHoleCards() != null) {
            cards = player.getHoleCards().getCards().stream()
                .map(CardDto::from)
                .toList();
        }
        return new PlayerDto(
            player.getName(),
            player.getChips(),
            player.getCurrentBet(),
            player.getPosition(),
            player.isFolded(),
            player.isAllIn(),
            player instanceof poker.model.HumanPlayer,
            cards
        );
    }
}
