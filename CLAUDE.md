# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A poker training simulator with AI opponents. Players learn Texas Hold'em strategy through interactive gameplay with real-time feedback and decision analysis. Built as a multi-module Gradle project with Spring Boot backend and React frontend.

## Build Commands

```bash
# Build entire project (backend + frontend)
./gradlew build

# Build backend only
./gradlew :backend:build

# Build frontend only
./gradlew :frontend:build

# Run tests
./gradlew test

# Run single test class
./gradlew :backend:test --tests "ClassName"

# Run backend server (http://localhost:8080)
./gradlew :backend:bootRun

# Run frontend dev server
cd frontend && npm run dev

# Frontend production build
cd frontend && npm run build
```

## Architecture

### Multi-Module Structure

- **Root**: Gradle multi-project configuration (`settings.gradle` includes `backend` and `frontend`)
- **Backend**: Spring Boot 3.4.1 + Java 21 REST API with in-memory game sessions
- **Frontend**: React 18 + TypeScript + Vite SPA

### Backend Core Components

The backend follows a layered architecture separating game logic from web concerns:

**Game Engine (`poker.engine`)**
- `GameEngine`: Orchestrates complete poker hands through all betting rounds
- `DealerManager`: Handles card dealing (hole cards, flop, turn, river)
- `BettingManager`: Manages blinds, validates actions, tracks pot and betting rounds

**Model Layer (`poker.model`)**
- `GameState`: Immutable snapshot of current game (players, pot, community cards, stage)
- `Player`: Base class with `HumanPlayer` and `AIPlayer` subclasses
- `Action`: Poker actions (fold, check, call, bet, raise, all-in)
- `Card`, `Deck`: Card representations and shuffling
- `Position`: Table positions (BTN, SB, BB, UTG, etc.)

**AI System (`poker.ai`)**
- `AIStrategy`: Interface for different AI personalities
- Implementations: `TightPassiveAI`, `LooseAggressiveAI`, `CallingStationAI`
- AI players use position-based hand strength evaluation

**Analytics (`poker.analytics`)**
- `DecisionAnalyzer`: Provides real-time feedback on human player decisions
- `FeedbackGenerator`: Generates Russian-language coaching tips
- `SessionStats`: Tracks performance metrics

**Evaluation (`poker.evaluation`)**
- `HandEvaluator`: Ranks 5-card poker hands (Royal Flush → High Card)
- `WinnerDeterminer`: Resolves showdowns, handles side pots and splits

**Web Layer (`poker.web`)**
- `GameController`: REST endpoints for game CRUD and actions
- `GameSession`: Manages game state on separate thread, queues human actions
- DTOs in `poker.web.dto`: JSON representations for API

### Frontend Architecture

**State Management**
- Top-level state in `App.tsx`: `sessionId` and `gameState`
- API calls in `api.ts` communicate with backend REST endpoints

**Components**
- `GameSetup`: Initial configuration (player count, starting chips, AI type)
- `PokerTable`: Main game view showing all players and community cards
- `PlayerSeat`: Individual player display with cards, chips, and position
- `CardDisplay`: Visual card rendering

**Game Flow**
1. Setup → POST `/api/game/create` → receive `sessionId`
2. Start hand → POST `/api/game/{sessionId}/start`
3. User action → POST `/api/game/{sessionId}/action`
4. Backend processes AI turns automatically
5. Frontend polls or receives updated `GameState`

### Key Patterns

**Session Management**:
- Backend uses `ConcurrentHashMap<String, GameSession>` for in-memory sessions
- Each `GameSession` runs game loop on background thread
- Human actions submitted via queue, processed synchronously in game flow

**Observer Pattern**:
- `GameEngine` uses listeners (`stateUpdateListener`, `messageListener`, `analysisListener`)
- `GameSession` implements these to capture game events and expose via DTOs

**Position-Aware Gameplay**:
- Positions dynamically assigned based on player count
- Button rotates each hand
- Betting order follows position (UTG → BTN)
- AI strategy tightens in early positions, loosens near button

## Russian Language Support

Game messages, feedback, and position names use Russian:
- Position names: "Баттон" (Button), "Малый блайнд" (SB), "Большой блайнд" (BB)
- Game stages: "Префлоп", "Флоп", "Тёрн", "Ривер"
- Actions: "Фолд", "Колл", "Рейз", "Ставка", "Чек", "Ва-банк"

See `poker-guide-80-20.md` for complete Russian poker terminology and strategy guide.

## Development Notes

- Java 21 required (uses records, pattern matching, switch expressions)
- No test suite currently exists (`gradlew test` will pass with no tests)
- Frontend uses TypeScript strict mode
- Backend runs on port 8080, configure via `application.properties`
- CORS not configured - frontend must run through backend or configure separately
