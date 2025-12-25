import { useState, useEffect } from 'react'
import { motion } from 'motion/react'
import { createGame, startHand } from '../api'
import { GameState } from '../types'
import './GameSetup.css'

interface Props {
  onGameStart: (sessionId: string, state: GameState) => void
}

const aiTypes = [
  { value: 'mixed', label: 'Смешанный', desc: 'Разные стили игры за столом' },
  { value: 'calling_station', label: 'Calling Station', desc: 'Коллирует слишком часто' },
  { value: 'tight_passive', label: 'Тайтовый', desc: 'Играет мало рук, боится рейзить' },
  { value: 'loose_aggressive', label: 'Агрессивный', desc: 'Много рейзит, играет слабые руки' },
]

const chipStacks = [
  { value: 500, label: '500' },
  { value: 1000, label: '1,000' },
  { value: 2000, label: '2,000' },
  { value: 5000, label: '5,000' },
]

const blindLevels = [
  { small: 10, big: 20, label: '10/20' },
  { small: 25, big: 50, label: '25/50' },
  { small: 50, big: 100, label: '50/100' },
  { small: 100, big: 200, label: '100/200' },
]

export function GameSetup({ onGameStart }: Props) {
  const [playerCount, setPlayerCount] = useState(6)
  const [humanPosition, setHumanPosition] = useState(0)
  const [startingChips, setStartingChips] = useState(1000)
  const [blindLevel, setBlindLevel] = useState(blindLevels[0])
  const [aiType, setAiType] = useState('mixed')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (humanPosition >= playerCount) {
      setHumanPosition(0)
    }
  }, [playerCount, humanPosition])

  const handleStart = async () => {
    setLoading(true)
    try {
      const sessionId = await createGame({
        playerCount,
        humanPosition,
        startingChips,
        smallBlind: blindLevel.small,
        bigBlind: blindLevel.big,
        aiType
      })

      const state = await startHand(sessionId)
      onGameStart(sessionId, state)
    } catch (error) {
      console.error('Failed to start game:', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="setup-container">
      {/* Decorative cards background */}
      <div className="setup-bg-cards">
        <motion.div
          className="bg-card bg-card-1"
          initial={{ rotate: -15, opacity: 0 }}
          animate={{ rotate: -15, opacity: 0.1 }}
          transition={{ duration: 1, delay: 0.2 }}
        />
        <motion.div
          className="bg-card bg-card-2"
          initial={{ rotate: 10, opacity: 0 }}
          animate={{ rotate: 10, opacity: 0.08 }}
          transition={{ duration: 1, delay: 0.4 }}
        />
        <motion.div
          className="bg-card bg-card-3"
          initial={{ rotate: 25, opacity: 0 }}
          animate={{ rotate: 25, opacity: 0.06 }}
          transition={{ duration: 1, delay: 0.6 }}
        />
      </div>

      <motion.div
        className="setup-panel"
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6, ease: [0.4, 0, 0.2, 1] }}
      >
        {/* Header */}
        <div className="setup-header">
          <motion.div
            className="logo-icon"
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ duration: 0.5, delay: 0.3, type: 'spring' }}
          >
            <span className="spade">♠</span>
          </motion.div>
          <motion.h1
            className="setup-title"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.4 }}
          >
            Покер Тренер
          </motion.h1>
          <motion.p
            className="setup-subtitle"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.5 }}
          >
            Texas Hold'em
          </motion.p>
          <div className="gold-line" />
        </div>

        {/* Form */}
        <div className="setup-form">
          {/* Player Count */}
          <motion.div
            className="form-section"
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.5 }}
          >
            <label className="form-label">Игроков за столом</label>
            <div className="player-count-grid">
              {[2, 3, 4, 5, 6].map((n) => (
                <motion.button
                  key={n}
                  className={`count-btn ${playerCount === n ? 'active' : ''}`}
                  onClick={() => setPlayerCount(n)}
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                >
                  {n}
                </motion.button>
              ))}
            </div>
          </motion.div>

          {/* Position */}
          <motion.div
            className="form-section"
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.6 }}
          >
            <label className="form-label">Ваша позиция</label>
            <div className="position-select">
              {Array.from({ length: playerCount }, (_, i) => (
                <motion.button
                  key={i}
                  className={`position-btn ${humanPosition === i ? 'active' : ''}`}
                  onClick={() => setHumanPosition(i)}
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                >
                  {i + 1}
                </motion.button>
              ))}
            </div>
          </motion.div>

          {/* Starting Chips */}
          <motion.div
            className="form-section"
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.7 }}
          >
            <label className="form-label">Начальный стек</label>
            <div className="chips-grid">
              {chipStacks.map((stack) => (
                <motion.button
                  key={stack.value}
                  className={`chip-btn ${startingChips === stack.value ? 'active' : ''}`}
                  onClick={() => setStartingChips(stack.value)}
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                >
                  <span className="chip-icon">●</span>
                  <span className="chip-value">{stack.label}</span>
                </motion.button>
              ))}
            </div>
          </motion.div>

          {/* Blinds */}
          <motion.div
            className="form-section"
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.75 }}
          >
            <label className="form-label">Блайнды</label>
            <div className="blinds-grid">
              {blindLevels.map((level) => (
                <motion.button
                  key={level.label}
                  className={`blind-btn ${blindLevel.label === level.label ? 'active' : ''}`}
                  onClick={() => setBlindLevel(level)}
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                >
                  <span className="blind-value">{level.label}</span>
                </motion.button>
              ))}
            </div>
          </motion.div>

          {/* AI Type */}
          <motion.div
            className="form-section"
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.8 }}
          >
            <label className="form-label">Стиль соперников</label>
            <div className="ai-type-grid">
              {aiTypes.map((type) => (
                <motion.button
                  key={type.value}
                  className={`ai-btn ${aiType === type.value ? 'active' : ''}`}
                  onClick={() => setAiType(type.value)}
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                >
                  <span className="ai-label">{type.label}</span>
                  <span className="ai-desc">{type.desc}</span>
                </motion.button>
              ))}
            </div>
          </motion.div>
        </div>

        {/* Start Button */}
        <motion.div
          className="start-section"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.9 }}
        >
          <motion.button
            className="start-btn"
            onClick={handleStart}
            disabled={loading}
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.98 }}
          >
            {loading ? (
              <span className="loading-text">
                <span className="loading-dot">●</span>
                <span className="loading-dot">●</span>
                <span className="loading-dot">●</span>
              </span>
            ) : (
              <>
                <span>Начать игру</span>
                <span className="btn-arrow">→</span>
              </>
            )}
          </motion.button>
        </motion.div>

        {/* Footer */}
        <div className="setup-footer">
          <span className="footer-suits">♠ ♥ ♦ ♣</span>
        </div>
      </motion.div>
    </div>
  )
}
