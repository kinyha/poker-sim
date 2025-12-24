export interface Card {
  rank: string
  suit: string
  display: string
}

export interface Player {
  name: string
  chips: number
  currentBet: number
  position: string
  folded: boolean
  allIn: boolean
  isHuman: boolean
  holeCards: Card[] | null
}

export interface GameState {
  players: Player[]
  communityCards: Card[]
  stage: string
  pot: number
  currentBet: number
  activePlayerIndex: number
  buttonPosition: number
  isHumanTurn: boolean
  handComplete: boolean
  resultMessage: string | null
  recommendation: string | null
  availableActions: string[]
}

export interface GameSetupConfig {
  playerCount: number
  humanPosition: number
  startingChips: number
  smallBlind: number
  bigBlind: number
  aiType?: string
}

export type ActionType = 'FOLD' | 'CHECK' | 'CALL' | 'BET' | 'RAISE' | 'ALL_IN'
