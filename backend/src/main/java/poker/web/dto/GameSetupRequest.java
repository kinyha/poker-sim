package poker.web.dto;

public record GameSetupRequest(
    int playerCount,
    int humanPosition,
    int startingChips,
    int smallBlind,
    int bigBlind,
    String aiType
) {
    public GameSetupRequest {
        if (playerCount < 2 || playerCount > 9) {
            throw new IllegalArgumentException("Player count must be between 2 and 9");
        }
        if (humanPosition < 0 || humanPosition >= playerCount) {
            throw new IllegalArgumentException("Invalid human position");
        }
        if (aiType == null) {
            aiType = "mixed";
        }
    }
}
