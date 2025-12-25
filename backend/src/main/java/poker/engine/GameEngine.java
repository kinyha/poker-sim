package poker.engine;

import poker.model.*;
import poker.analytics.*;
import poker.evaluation.WinnerDeterminer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class GameEngine {
    private final DealerManager dealer;
    private final BettingManager betting;
    private final WinnerDeterminer showdown;
    private final DecisionAnalyzer analyzer;
    private final List<Player> players;
    private GameState currentState;
    private int buttonPosition;

    private BiFunction<GameState, Player, Action> humanActionProvider;
    private Consumer<GameState> stateUpdateListener;
    private Consumer<String> messageListener;
    private Consumer<HandAnalysis> analysisListener;

    public GameEngine(List<Player> players, int smallBlind, int bigBlind) {
        this.players = new ArrayList<>(players);
        this.dealer = new DealerManager();
        this.betting = new BettingManager(smallBlind, bigBlind);
        this.showdown = new WinnerDeterminer();
        this.analyzer = new DecisionAnalyzer();
        this.buttonPosition = 0;
    }

    public void setHumanActionProvider(BiFunction<GameState, Player, Action> provider) {
        this.humanActionProvider = provider;
    }

    public void setStateUpdateListener(Consumer<GameState> listener) {
        this.stateUpdateListener = listener;
    }

    public void setMessageListener(Consumer<String> listener) {
        this.messageListener = listener;
    }

    public void setAnalysisListener(Consumer<HandAnalysis> listener) {
        this.analysisListener = listener;
    }

    public HandResult playHand() {
        initializeHand();

        betting.postBlinds(currentState);
        notifyStateUpdate();

        dealer.dealHoleCards(players);
        notifyStateUpdate();

        notifyMessage("=== " + currentState.getStage().getRussianName() + " ===");
        playBettingRound();
        if (isHandOver()) {
            return resolveHand();
        }

        currentState.advanceStage();
        dealer.dealFlop(currentState);
        notifyMessage("=== " + currentState.getStage().getRussianName() + " ===");
        notifyStateUpdate();
        playBettingRound();
        if (isHandOver()) {
            return resolveHand();
        }

        currentState.advanceStage();
        dealer.dealTurn(currentState);
        notifyMessage("=== " + currentState.getStage().getRussianName() + " ===");
        notifyStateUpdate();
        playBettingRound();
        if (isHandOver()) {
            return resolveHand();
        }

        currentState.advanceStage();
        dealer.dealRiver(currentState);
        notifyMessage("=== " + currentState.getStage().getRussianName() + " ===");
        notifyStateUpdate();
        playBettingRound();

        return resolveHand();
    }

    private void initializeHand() {
        for (Player player : players) {
            player.resetForNewHand();
        }

        // Filter out players with 0 chips (eliminated players)
        List<Player> activePlayers = players.stream()
            .filter(p -> p.getChips() > 0)
            .toList();

        currentState = new GameState(activePlayers, betting.getSmallBlind(), betting.getBigBlind());

        // Translate button position from full player list to active player list
        int activeButtonPos = findActiveButtonPosition(activePlayers);
        currentState.setButtonPosition(activeButtonPos);

        assignPositions();
    }

    private int findActiveButtonPosition(List<Player> activePlayers) {
        // Find the button position among active players
        // The button should be on the first active player at or after the engine's buttonPosition
        for (int i = 0; i < activePlayers.size(); i++) {
            if (players.indexOf(activePlayers.get(i)) >= buttonPosition) {
                return i;
            }
        }
        // If no player found after buttonPosition, wrap around to first active player
        return 0;
    }

    private void assignPositions() {
        // Only assign positions to active players (those with chips > 0)
        List<Player> activePlayers = currentState.getPlayers();
        if (activePlayers.isEmpty()) return;

        List<Position> positions = Position.getPositionsForPlayerCount(activePlayers.size());

        int activeButtonPos = currentState.getButtonPosition();

        for (int i = 0; i < activePlayers.size(); i++) {
            // relativePos: 0=button, 1=SB, 2=BB, etc.
            int relativePos = (i - activeButtonPos + activePlayers.size()) % activePlayers.size();
            // positions list is [SB, BB, ..., BTN], so BTN is at the end
            // Map: 0 -> BTN (last), 1 -> SB (first), 2 -> BB (second), etc.
            int posIndex = (relativePos + positions.size() - 1) % positions.size();
            activePlayers.get(i).setPosition(positions.get(posIndex));
        }
    }

    private void playBettingRound() {
        currentState.resetBettingRound();

        if (currentState.getStage() == GameStage.PREFLOP) {
            int utg = (currentState.getButtonPosition() + 3) % currentState.getPlayers().size();
            currentState.setActivePlayerIndex(utg);
        }

        int maxIterations = currentState.getPlayers().size() * 10; // Safety limit
        int iterations = 0;

        while (!betting.isRoundComplete(currentState) && currentState.getPlayersInHand() > 1) {
            iterations++;
            if (iterations > maxIterations) {
                break; // Safety exit
            }

            // No one can act - exit
            if (!currentState.hasPlayersWhoCanAct()) {
                break;
            }

            Player activePlayer = currentState.getCurrentPlayer();

            if (!activePlayer.canAct()) {
                currentState.nextPlayer();
                continue;
            }

            Action action = getPlayerAction(activePlayer);

            if (activePlayer instanceof HumanPlayer) {
                HandAnalysis analysis = analyzer.analyzeDecision(activePlayer, currentState, action);
                notifyAnalysis(analysis);
            }

            betting.applyAction(currentState, activePlayer, action);

            notifyMessage(activePlayer.getName() + ": " + action.toRussianString());
            notifyStateUpdate();

            currentState.nextPlayer();
        }
    }

    private Action getPlayerAction(Player player) {
        if (player instanceof HumanPlayer && humanActionProvider != null) {
            return humanActionProvider.apply(currentState, player);
        }
        return player.decideAction(currentState);
    }

    private boolean isHandOver() {
        return currentState.getPlayersInHand() <= 1;
    }

    private HandResult resolveHand() {
        WinnerDeterminer.ShowdownResult result = showdown.resolveShowdown(currentState);

        for (Player winner : result.winners()) {
            int winnings = result.winnings().get(winner);
            winner.win(winnings);
        }

        notifyMessage("\n" + result.getSummary());

        moveButton();

        return new HandResult(
            result.winners(),
            result.winnings(),
            result.winningHand(),
            result.isSplit()
        );
    }

    private void moveButton() {
        buttonPosition = (buttonPosition + 1) % players.size();
    }

    private void notifyStateUpdate() {
        if (stateUpdateListener != null) {
            stateUpdateListener.accept(currentState);
        }
    }

    private void notifyMessage(String message) {
        if (messageListener != null) {
            messageListener.accept(message);
        }
    }

    private void notifyAnalysis(HandAnalysis analysis) {
        if (analysisListener != null) {
            analysisListener.accept(analysis);
        }
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getButtonPosition() {
        return buttonPosition;
    }

    public record HandResult(
        List<Player> winners,
        java.util.Map<Player, Integer> winnings,
        Hand winningHand,
        boolean isSplit
    ) {}
}
