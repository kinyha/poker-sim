import { useState, useEffect } from 'react'
import { motion, AnimatePresence } from 'motion/react'
import { submitAction, getGameState, startHand } from '../api'
import { GameState, ActionType } from '../types'
import { CardDisplay } from './CardDisplay'
import { PlayerSeat } from './PlayerSeat'
import './PokerTable.css'

interface Props {
  sessionId: string
  gameState: GameState
  onStateUpdate: (state: GameState) => void
}

const stageNames: Record<string, string> = {
  'PREFLOP': 'Префлоп',
  'FLOP': 'Флоп',
  'TURN': 'Тёрн',
  'RIVER': 'Ривер',
  'SHOWDOWN': 'Шоудаун'
}

// Parse recommendation string into action and reasoning
function parseRecommendation(recommendation: string | null): { action: string; reasoning: string } | null {
  if (!recommendation) return null

  // Format: "Фолд: K6o - Мусор. Фолд! ..."
  const colonIndex = recommendation.indexOf(':')
  if (colonIndex === -1) {
    return { action: recommendation, reasoning: '' }
  }

  const action = recommendation.substring(0, colonIndex).trim()
  let reasoning = recommendation.substring(colonIndex + 1).trim()

  // Remove duplicate "Позиция:" and "Рекомендация:" parts if present
  reasoning = reasoning.replace(/\s*Позиция:.*$/i, '').trim()
  reasoning = reasoning.replace(/\s*Рекомендация:.*$/i, '').trim()

  return { action, reasoning }
}

export function PokerTable({ sessionId, gameState, onStateUpdate }: Props) {
  const [betAmount, setBetAmount] = useState(0)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (gameState.handComplete) return

    const pollState = setInterval(async () => {
      try {
        const state = await getGameState(sessionId)
        if (state) {
          onStateUpdate(state)
        }
      } catch (error) {
        console.error('Failed to poll state:', error)
      }
    }, 1000)

    return () => clearInterval(pollState)
  }, [sessionId, onStateUpdate, gameState.handComplete])

  const handleAction = async (type: ActionType, amount = 0) => {
    setLoading(true)
    try {
      const state = await submitAction(sessionId, type, amount)
      onStateUpdate(state)
    } catch (error) {
      console.error('Failed to submit action:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleNextHand = async () => {
    setLoading(true)
    try {
      const state = await startHand(sessionId)
      onStateUpdate(state)
    } catch (error) {
      console.error('Failed to start new hand:', error)
    } finally {
      setLoading(false)
    }
  }

  const humanPlayer = gameState.players.find(p => p.isHuman)
  const amountToCall = gameState.currentBet - (humanPlayer?.currentBet || 0)
  const canCheck = amountToCall === 0
  const minRaise = gameState.currentBet + 20

  return (
    <div className="table-container">
      {/* Top info bar */}
      <motion.div
        className="top-bar"
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        <div className="stage-badge">
          <span className="stage-label">Стадия</span>
          <span className="stage-value">{stageNames[gameState.stage] || gameState.stage}</span>
        </div>
        <div className="pot-display">
          <span className="pot-label">Банк</span>
          <motion.span
            className="pot-value"
            key={gameState.pot}
            initial={{ scale: 1.2 }}
            animate={{ scale: 1 }}
            transition={{ type: 'spring', stiffness: 300 }}
          >
            {gameState.pot.toLocaleString()}
          </motion.span>
        </div>
      </motion.div>

      {/* Main table */}
      <div className="table-wrapper">
        <motion.div
          className="poker-table"
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.6 }}
        >
          {/* Felt texture overlay */}
          <div className="felt-overlay" />

          {/* Table rail */}
          <div className="table-rail" />

          {/* Community cards area */}
          <div className="community-area">
            <AnimatePresence mode="popLayout">
              {gameState.communityCards.length > 0 ? (
                gameState.communityCards.map((card, i) => (
                  <motion.div
                    key={`${card.rank}-${card.suit}-${i}`}
                    initial={{ opacity: 0, y: -30, rotateY: 180 }}
                    animate={{ opacity: 1, y: 0, rotateY: 0 }}
                    exit={{ opacity: 0, scale: 0.8 }}
                    transition={{
                      duration: 0.4,
                      delay: i * 0.1,
                      type: 'spring',
                      stiffness: 200
                    }}
                  >
                    <CardDisplay card={card} size="large" />
                  </motion.div>
                ))
              ) : (
                <motion.div
                  className="waiting-cards"
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                >
                  <span className="waiting-icon">♠ ♥ ♦ ♣</span>
                  <span className="waiting-text">Ожидание карт...</span>
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          {/* Pot chips visualization */}
          {gameState.pot > 0 && (
            <motion.div
              className="pot-chips"
              initial={{ opacity: 0, scale: 0 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ type: 'spring', stiffness: 200 }}
            >
              <div className="chip-stack">
                <div className="chip chip-gold" />
                <div className="chip chip-gold" style={{ top: '-4px' }} />
                <div className="chip chip-gold" style={{ top: '-8px' }} />
              </div>
            </motion.div>
          )}

          {/* Player seats */}
          <div className="players-container">
            {gameState.players.map((player, i) => (
              <PlayerSeat
                key={i}
                player={player}
                isActive={i === gameState.activePlayerIndex && !gameState.handComplete}
                isButton={i === gameState.buttonPosition}
                position={i}
                total={gameState.players.length}
              />
            ))}
          </div>
        </motion.div>
      </div>

      {/* Result overlay */}
      <AnimatePresence>
        {gameState.handComplete && (
          <motion.div
            className="result-overlay"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
          >
            <motion.div
              className="result-card"
              initial={{ scale: 0.8, y: 50 }}
              animate={{ scale: 1, y: 0 }}
              transition={{ type: 'spring', stiffness: 200 }}
            >
              <div className="result-header">
                <span className="result-crown">♔</span>
                <h2 className="result-title">Раздача завершена</h2>
              </div>

              {/* Community cards in result */}
              {gameState.communityCards.length > 0 && (
                <div className="result-community">
                  <span className="result-community-label">Карты на столе</span>
                  <div className="result-cards">
                    {gameState.communityCards.map((card, i) => (
                      <CardDisplay key={`result-${card.rank}-${card.suit}-${i}`} card={card} size="medium" />
                    ))}
                  </div>
                </div>
              )}

              <div className="result-message">
                {gameState.resultMessage || 'Готово к следующей раздаче'}
              </div>
              <motion.button
                className="btn-next"
                onClick={handleNextHand}
                disabled={loading}
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
              >
                {loading ? (
                  <span className="btn-loading">●●●</span>
                ) : (
                  <>Следующая раздача</>
                )}
              </motion.button>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Action panel */}
      <AnimatePresence>
        {!gameState.handComplete && gameState.isHumanTurn && (
          <motion.div
            className="action-panel"
            initial={{ opacity: 0, y: 50 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 50 }}
            transition={{ type: 'spring', stiffness: 200 }}
          >
            {/* Recommendation - 3 blocks */}
            {gameState.recommendation && (() => {
              const parsed = parseRecommendation(gameState.recommendation)
              if (!parsed) return null
              return (
                <motion.div
                  className="recommendation-grid"
                  initial={{ opacity: 0, y: -10 }}
                  animate={{ opacity: 1, y: 0 }}
                >
                  <div className="rec-block rec-action-block">
                    <span className="rec-block-label">Действие</span>
                    <span className="rec-block-value rec-action-value">{parsed.action}</span>
                  </div>
                  <div className="rec-block rec-comment-block">
                    <span className="rec-block-label">Комментарий</span>
                    <span className="rec-block-value">{parsed.reasoning || '—'}</span>
                  </div>
                  <div className="rec-block rec-position-block">
                    <span className="rec-block-label">Позиция</span>
                    <span className="rec-block-value">{humanPlayer?.position || '—'}</span>
                  </div>
                </motion.div>
              )
            })()}

            {/* Action buttons */}
            <div className="action-buttons">
              <motion.button
                className="action-btn btn-fold"
                onClick={() => handleAction('FOLD')}
                disabled={loading}
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
              >
                <span className="btn-label">Фолд</span>
              </motion.button>

              {canCheck ? (
                <motion.button
                  className="action-btn btn-check"
                  onClick={() => handleAction('CHECK')}
                  disabled={loading}
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                >
                  <span className="btn-label">Чек</span>
                </motion.button>
              ) : (
                <motion.button
                  className="action-btn btn-call"
                  onClick={() => handleAction('CALL')}
                  disabled={loading}
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                >
                  <span className="btn-label">Колл</span>
                  <span className="btn-amount">{amountToCall}</span>
                </motion.button>
              )}

              <div className="raise-group">
                <input
                  type="number"
                  className="raise-input"
                  min={minRaise}
                  max={humanPlayer?.chips || 1000}
                  value={betAmount || minRaise}
                  onChange={(e) => setBetAmount(Number(e.target.value))}
                />
                <motion.button
                  className="action-btn btn-raise"
                  onClick={() => handleAction('RAISE', betAmount || minRaise)}
                  disabled={loading}
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                >
                  <span className="btn-label">Рейз</span>
                  <span className="btn-amount">{betAmount || minRaise}</span>
                </motion.button>
              </div>

              <motion.button
                className="action-btn btn-allin"
                onClick={() => handleAction('ALL_IN')}
                disabled={loading}
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
              >
                <span className="btn-label">Олл-ин</span>
                <span className="btn-amount">{humanPlayer?.chips}</span>
              </motion.button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Waiting indicator */}
      <AnimatePresence>
        {!gameState.handComplete && !gameState.isHumanTurn && (
          <motion.div
            className="waiting-indicator"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
          >
            <div className="waiting-spinner" />
            <span>Ход соперника...</span>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}
