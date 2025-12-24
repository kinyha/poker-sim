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
    public static GameStateDto from(GameState state, List<Player> allPlayers, Player humanPlayer, boolean handComplete, String resultMessage, String recommendation) {
        boolean isHumanTurn = !handComplete && state != null && state.getCurrentPlayer() == humanPlayer && humanPlayer.canAct();

        // Use all players (including eliminated) for display
        List<PlayerDto> playerDtos = allPlayers.stream()
            .map(p -> PlayerDto.from(p, p == humanPlayer || (state != null && state.getStage() == GameStage.SHOWDOWN) || handComplete))
            .toList();

        List<CardDto> communityCards = state != null ? state.getCommunityCards().stream()
            .map(CardDto::from)
            .toList() : List.of();

        return new GameStateDto(
            playerDtos,
            communityCards,
            state != null ? state.getStage() : GameStage.PREFLOP,
            state != null ? state.getPot().getTotal() : 0,
            state != null ? state.getCurrentBet() : 0,
            state != null ? state.getActivePlayerIndex() : -1,
            state != null ? state.getButtonPosition() : 0,
            isHumanTurn,
            handComplete,
            resultMessage,
            recommendation,
            List.of()
        );
    }

    public static GameStateDto from(GameState state, Player humanPlayer, boolean handComplete, String resultMessage, String recommendation) {
        return from(state, state != null ? state.getPlayers() : List.of(), humanPlayer, handComplete, resultMessage, recommendation);
    }

    public static GameStateDto from(GameState state, Player humanPlayer) {
        return from(state, humanPlayer, false, null, null);
    }
}
