import { Card } from '../types'
import './CardDisplay.css'

interface Props {
  card: Card
  hidden?: boolean
}

const suitSymbols: Record<string, string> = {
  HEARTS: '♥',
  DIAMONDS: '♦',
  CLUBS: '♣',
  SPADES: '♠'
}

const suitColors: Record<string, string> = {
  HEARTS: '#e74c3c',
  DIAMONDS: '#e74c3c',
  CLUBS: '#2c3e50',
  SPADES: '#2c3e50'
}

const rankDisplay: Record<string, string> = {
  TWO: '2', THREE: '3', FOUR: '4', FIVE: '5',
  SIX: '6', SEVEN: '7', EIGHT: '8', NINE: '9',
  TEN: '10', JACK: 'J', QUEEN: 'Q', KING: 'K', ACE: 'A'
}

export function CardDisplay({ card, hidden }: Props) {
  if (hidden) {
    return <div className="card card-hidden">🂠</div>
  }

  const suit = suitSymbols[card.suit] || card.suit
  const color = suitColors[card.suit] || '#000'
  const rank = rankDisplay[card.rank] || card.rank

  return (
    <div className="card" style={{ color }}>
      <span className="rank">{rank}</span>
      <span className="suit">{suit}</span>
    </div>
  )
}
