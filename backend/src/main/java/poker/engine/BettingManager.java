package poker.engine;

import poker.model.*;

import java.util.List;

public class BettingManager {
    private final int smallBlind;
    private final int bigBlind;

    public BettingManager(int smallBlind, int bigBlind) {
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
    }

    public void postBlinds(GameState state) {
        Player sbPlayer = state.getSmallBlindPlayer();
        Player bbPlayer = state.getBigBlindPlayer();

        int sbAmount = Math.min(smallBlind, sbPlayer.getChips());
        int bbAmount = Math.min(bigBlind, bbPlayer.getChips());

        sbPlayer.bet(sbAmount);
        bbPlayer.bet(bbAmount);

        state.getPot().add(sbAmount + bbAmount);
        state.setCurrentBet(bigBlind);
    }

    public void applyAction(GameState state, Player player, Action action) {
        int toCall = state.getAmountToCall(player);

        switch (action.getType()) {
            case FOLD -> player.fold();

            case CHECK -> {
            }

            case CALL -> {
                int callAmount = Math.min(toCall, player.getChips());
                player.bet(callAmount);
                state.getPot().add(callAmount);
            }

            case BET -> {
                int betAmount = Math.min(action.getAmount(), player.getChips());
                player.bet(betAmount);
                state.getPot().add(betAmount);
                state.setCurrentBet(player.getCurrentBet());
                state.setLastRaiserIndex(state.getActivePlayerIndex());
            }

            case RAISE -> {
                int raiseTotal = action.getAmount();
                int toAdd = raiseTotal - player.getCurrentBet();
                int actualAdd = Math.min(toAdd, player.getChips());
                player.bet(actualAdd);
                state.getPot().add(actualAdd);
                state.setCurrentBet(player.getCurrentBet());
                state.setLastRaiserIndex(state.getActivePlayerIndex());
            }

            case ALL_IN -> {
                int allInAmount = player.getChips();
                player.bet(allInAmount);
                state.getPot().add(allInAmount);
                if (player.getCurrentBet() > state.getCurrentBet()) {
                    state.setCurrentBet(player.getCurrentBet());
                    state.setLastRaiserIndex(state.getActivePlayerIndex());
                }
            }
        }
    }

    public boolean isRoundComplete(GameState state) {
        // Only one player left - everyone else folded
        if (state.getPlayersInHand() <= 1) {
            return true;
        }

        // No one can act (all folded or all-in)
        if (!state.hasPlayersWhoCanAct()) {
            return true;
        }

        List<Player> activePlayers = state.getActivePlayers();
        if (activePlayers.isEmpty()) {
            return true;
        }

        int targetBet = state.getCurrentBet();

        // All players who can act must have matched the bet
        boolean allMatched = activePlayers.stream()
            .allMatch(p -> p.getCurrentBet() == targetBet);

        // Everyone must have had a chance to act
        boolean allActed = state.getActionsThisRound() >= activePlayers.size();

        return allMatched && allActed;
    }

    public int getSmallBlind() {
        return smallBlind;
    }

    public int getBigBlind() {
        return bigBlind;
    }

    public boolean canCheck(GameState state, Player player) {
        return state.getAmountToCall(player) == 0;
    }

    public boolean canBet(GameState state) {
        return state.getCurrentBet() == 0;
    }

    public int getMinBet(GameState state) {
        return bigBlind;
    }

    public int getMinRaise(GameState state) {
        return state.getCurrentBet() + bigBlind;
    }
}
