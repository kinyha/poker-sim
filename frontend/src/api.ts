import { GameSetupConfig, GameState, ActionType } from './types'

const API_BASE = import.meta.env.VITE_API_URL || '/api/game'

export async function createGame(config: GameSetupConfig): Promise<string> {
  const response = await fetch(`${API_BASE}/create`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(config)
  })
  const data = await response.json()
  return data.sessionId
}

export async function startHand(sessionId: string): Promise<GameState> {
  const response = await fetch(`${API_BASE}/${sessionId}/start`, {
    method: 'POST'
  })
  return response.json()
}

export async function getGameState(sessionId: string): Promise<GameState> {
  const response = await fetch(`${API_BASE}/${sessionId}/state`)
  return response.json()
}

export async function submitAction(
  sessionId: string,
  type: ActionType,
  amount: number = 0
): Promise<GameState> {
  const response = await fetch(`${API_BASE}/${sessionId}/action`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ type, amount })
  })
  return response.json()
}
