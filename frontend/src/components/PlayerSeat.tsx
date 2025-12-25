import { motion } from 'motion/react'
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
  const rx = 48 // percentage of container width
  const ry = 46 // percentage of container height
  const x = 50 + rx * Math.cos(angle)
  const y = 50 + ry * Math.sin(angle)

  const isEliminated = player.chips === 0

  return (
    <motion.div
      className={`player-seat ${isActive ? 'active' : ''} ${player.folded ? 'folded' : ''} ${isEliminated ? 'eliminated' : ''} ${player.isHuman ? 'human' : ''}`}
      style={{
        left: `${x}%`,
        top: `${y}%`,
      }}
      initial={{ opacity: 0, scale: 0.8 }}
      animate={{ opacity: 1, scale: 1 }}
      transition={{ duration: 0.4, delay: position * 0.1 }}
    >
      {/* Dealer button */}
      {isButton && (
        <motion.div
          className="dealer-btn"
          initial={{ scale: 0 }}
          animate={{ scale: 1 }}
          transition={{ type: 'spring', stiffness: 300 }}
        >
          BTN
        </motion.div>
      )}

      {/* Active indicator ring */}
      {isActive && (
        <motion.div
          className="active-ring"
          initial={{ opacity: 0, scale: 0.8 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.3 }}
        />
      )}

      {/* Player cards */}
      <div className="seat-cards">
        {player.holeCards ? (
          player.holeCards.map((card, i) => (
            <motion.div
              key={i}
              initial={{ opacity: 0, y: -10, rotateY: 180 }}
              animate={{ opacity: 1, y: 0, rotateY: 0 }}
              transition={{ delay: i * 0.15, duration: 0.4 }}
            >
              <CardDisplay card={card} size="small" />
            </motion.div>
          ))
        ) : (
          <>
            <CardDisplay card={{ rank: '', suit: '', display: '' }} hidden size="small" />
            <CardDisplay card={{ rank: '', suit: '', display: '' }} hidden size="small" />
          </>
        )}
      </div>

      {/* Player info panel */}
      <div className="seat-info">
        <div className="seat-name-row">
          <span className="seat-name">{player.name}</span>
          {player.isHuman && <span className="human-badge">ВЫ</span>}
        </div>

        <div className="seat-position">{player.position}</div>

        <motion.div
          className="seat-chips"
          key={player.chips}
          initial={{ scale: 1.1 }}
          animate={{ scale: 1 }}
        >
          <span className="chip-icon">●</span>
          <span className="chip-amount">{player.chips.toLocaleString()}</span>
        </motion.div>

        {/* Current bet */}
        {player.currentBet > 0 && (
          <motion.div
            className="seat-bet"
            initial={{ opacity: 0, y: 5 }}
            animate={{ opacity: 1, y: 0 }}
          >
            <span className="bet-label">Ставка</span>
            <span className="bet-value">{player.currentBet}</span>
          </motion.div>
        )}

        {/* Status badges */}
        {isEliminated ? (
          <div className="status-badge eliminated">Выбыл</div>
        ) : (
          <>
            {player.folded && <div className="status-badge folded">Фолд</div>}
            {player.allIn && (
              <motion.div
                className="status-badge allin"
                animate={{ scale: [1, 1.05, 1] }}
                transition={{ repeat: Infinity, duration: 1.5 }}
              >
                All-In
              </motion.div>
            )}
          </>
        )}
      </div>
    </motion.div>
  )
}
