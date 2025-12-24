package poker.web.dto;

import poker.model.GameState;
import poker.model.GameStage;
import poker.model.Player;

import java.util.List;

public record GameStateDto(
    List<PlayerDto> players,
    List<CardDto> communityCards,
    GameStage stage,
    int pot,
    int currentBet,
    int activePlayerIndex,
    int buttonPosition,
    boolean isHumanTurn,
    boolean handComplete,
    String resultMessage,
    String recommendation,
    List<String> availableActions
) {
    public static GameStateDto from(GameState state, Player humanPlayer, boolean handComplete, String resultMessage, String recommendation) {
        boolean isHumanTurn = !handComplete && state.getCurrentPlayer() == humanPlayer && humanPlayer.canAct();

        List<PlayerDto> playerDtos = state.getPlayers().stream()
            .map(p -> PlayerDto.from(p, p == humanPlayer || state.getStage() == GameStage.SHOWDOWN || handComplete))
            .toList();

        List<CardDto> communityCards = state.getCommunityCards().stream()
            .map(CardDto::from)
            .toList();

        return new GameStateDto(
            playerDtos,
            communityCards,
            state.getStage(),
            state.getPot().getTotal(),
            state.getCurrentBet(),
            state.getActivePlayerIndex(),
            state.getButtonPosition(),
            isHumanTurn,
            handComplete,
            resultMessage,
            recommendation,
            List.of()
        );
    }

    public static GameStateDto from(GameState state, Player humanPlayer) {
        return from(state, humanPlayer, false, null, null);
    }
}
