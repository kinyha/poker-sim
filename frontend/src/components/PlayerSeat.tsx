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
  // Calculate position around the ellipse (updated for 900x500 table)
  const angle = (position / total) * 2 * Math.PI - Math.PI / 2
  const rx = 420 // horizontal radius (increased from 340)
  const ry = 220 // vertical radius (increased from 180)
  const x = 450 + rx * Math.cos(angle) // center x (900/2)
  const y = 250 + ry * Math.sin(angle) // center y (500/2)

  return (
    <div
      className={`player-seat ${isActive ? 'active' : ''} ${player.folded ? 'folded' : ''} ${player.chips === 0 ? 'eliminated' : ''}`}
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
        {player.chips === 0 ? (
          <div className="status-eliminated">ВЫБЫЛ</div>
        ) : (
          <>
            {player.folded && <div className="status-folded">ФОЛД</div>}
            {player.allIn && <div className="status-allin">ALL-IN</div>}
          </>
        )}
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
