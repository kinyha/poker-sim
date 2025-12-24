import { Player } from '../types'
import { CardDisplay } from './CardDisplay'
import './PlayerSeat.css'

interface Props {
  player: Player
  isActive: boolean
  isButton: boolean
  position: number
  total: number
}

export function PlayerSeat({ player, isActive, isButton, position, total }: Props) {
  // Calculate position around the ellipse
  const angle = (position / total) * 2 * Math.PI - Math.PI / 2
  const rx = 340 // horizontal radius
  const ry = 180 // vertical radius
  const x = 350 + rx * Math.cos(angle)
  const y = 200 + ry * Math.sin(angle)

  return (
    <div
      className={`player-seat ${isActive ? 'active' : ''} ${player.folded ? 'folded' : ''}`}
      style={{
        left: `${x}px`,
        top: `${y}px`,
        transform: 'translate(-50%, -50%)'
      }}
    >
      {isButton && <div className="dealer-button">D</div>}

      <div className="player-info">
        <div className="player-name">{player.name}</div>
        <div className="player-position">[{player.position}]</div>
        <div className="player-chips">${player.chips}</div>
        {player.currentBet > 0 && (
          <div className="player-bet">Ставка: ${player.currentBet}</div>
        )}
        {player.folded && <div className="status-folded">ФОЛД</div>}
        {player.allIn && <div className="status-allin">ALL-IN</div>}
      </div>

      <div className="player-cards">
        {player.holeCards ? (
          player.holeCards.map((card, i) => (
            <CardDisplay key={i} card={card} />
          ))
        ) : (
          <>
            <CardDisplay card={{ rank: '', suit: '', display: '' }} hidden />
            <CardDisplay card={{ rank: '', suit: '', display: '' }} hidden />
          </>
        )}
      </div>
    </div>
  )
}
