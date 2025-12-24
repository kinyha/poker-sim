import { useState } from 'react'
import { GameSetup } from './components/GameSetup'
import { PokerTable } from './components/PokerTable'
import { GameState } from './types'

function App() {
  const [sessionId, setSessionId] = useState<string | null>(null)
  const [gameState, setGameState] = useState<GameState | null>(null)

  const handleGameStart = (id: string, state: GameState) => {
    setSessionId(id)
    setGameState(state)
  }

  const handleStateUpdate = (state: GameState) => {
    setGameState(state)
  }

  return (
    <div className="app">
      {!sessionId ? (
        <GameSetup onGameStart={handleGameStart} />
      ) : (
        <PokerTable
          sessionId={sessionId}
          gameState={gameState!}
          onStateUpdate={handleStateUpdate}
        />
      )}
    </div>
  )
}

export default App
