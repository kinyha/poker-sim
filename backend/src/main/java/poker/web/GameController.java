package poker.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poker.model.Action;
import poker.model.ActionType;
import poker.web.dto.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createGame(@RequestBody GameSetupRequest request) {
        String sessionId = UUID.randomUUID().toString();

        GameSession session = new GameSession(
            sessionId,
            request.playerCount(),
            request.humanPosition(),
            request.startingChips(),
            request.smallBlind(),
            request.bigBlind(),
            request.aiType()
        );

        sessions.put(sessionId, session);

        return ResponseEntity.ok(Map.of("sessionId", sessionId));
    }

    @PostMapping("/{sessionId}/start")
    public ResponseEntity<GameStateDto> startHand(@PathVariable String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        session.startHand();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return ResponseEntity.ok(createStateDto(session));
    }

    @GetMapping("/{sessionId}/state")
    public ResponseEntity<GameStateDto> getState(@PathVariable String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        var state = session.getCurrentState();
        if (state == null) {
            return ResponseEntity.ok(null);
        }

        return ResponseEntity.ok(createStateDto(session));
    }

    @PostMapping("/{sessionId}/action")
    public ResponseEntity<GameStateDto> submitAction(
            @PathVariable String sessionId,
            @RequestBody ActionRequest request) {

        GameSession session = sessions.get(sessionId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        Action action = createAction(request.type(), request.amount());
        session.submitAction(action);

        // Wait for game to process (may complete the hand)
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            if (session.isHandComplete() || !session.isGameRunning()) {
                break;
            }
        }

        return ResponseEntity.ok(createStateDto(session));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId) {
        sessions.remove(sessionId);
        return ResponseEntity.ok().build();
    }

    private GameStateDto createStateDto(GameSession session) {
        return GameStateDto.from(
            session.getCurrentState(),
            session.getHumanPlayer(),
            session.isHandComplete(),
            session.getLastResultMessage(),
            session.getRecommendation()
        );
    }

    private Action createAction(ActionType type, int amount) {
        return switch (type) {
            case FOLD -> Action.fold();
            case CHECK -> Action.check();
            case CALL -> Action.call(amount);
            case BET -> Action.bet(amount);
            case RAISE -> Action.raise(amount);
            case ALL_IN -> Action.allIn(amount);
        };
    }
}
