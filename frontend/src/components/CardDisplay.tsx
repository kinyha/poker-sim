import { Card } from '../types'
import './CardDisplay.css'

interface Props {
  card: Card
  hidden?: boolean
  size?: 'small' | 'medium' | 'large'
}

const suitSymbols: Record<string, string> = {
  HEARTS: '♥',
  DIAMONDS: '♦',
  CLUBS: '♣',
  SPADES: '♠'
}

const suitColors: Record<string, string> = {
  HEARTS: 'red',
  DIAMONDS: 'red',
  CLUBS: 'black',
  SPADES: 'black'
}

const rankDisplay: Record<string, string> = {
  TWO: '2', THREE: '3', FOUR: '4', FIVE: '5',
  SIX: '6', SEVEN: '7', EIGHT: '8', NINE: '9',
  TEN: '10', JACK: 'J', QUEEN: 'Q', KING: 'K', ACE: 'A'
}

export function CardDisplay({ card, hidden, size = 'medium' }: Props) {
  if (hidden) {
    return (
      <div className={`card card-back size-${size}`}>
        <div className="card-back-pattern">
          <div className="pattern-diamond">♦</div>
        </div>
      </div>
    )
  }

  const suit = suitSymbols[card.suit] || card.suit
  const colorClass = suitColors[card.suit] || 'black'
  const rank = rankDisplay[card.rank] || card.rank

  return (
    <div className={`card size-${size} color-${colorClass}`}>
      <div className="card-inner">
        <div className="card-corner top-left">
          <span className="corner-rank">{rank}</span>
          <span className="corner-suit">{suit}</span>
        </div>
        <div className="card-center">
          <span className="center-suit">{suit}</span>
        </div>
        <div className="card-corner bottom-right">
          <span className="corner-rank">{rank}</span>
          <span className="corner-suit">{suit}</span>
        </div>
      </div>
    </div>
  )
}
