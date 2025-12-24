import { useState, useEffect } from 'react'
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
    <div className="poker-table-container">
      <div className="table-info">
        <span className="stage">{gameState.stage}</span>
        <span className="pot">Банк: ${gameState.pot}</span>
      </div>

      <div className="poker-table">
        <div className="community-cards">
          {gameState.communityCards.map((card, i) => (
            <CardDisplay key={i} card={card} />
          ))}
          {gameState.communityCards.length === 0 && (
            <div className="no-cards">Карты ещё не раздали</div>
          )}
        </div>

        <div className="players-circle">
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
      </div>

      {gameState.handComplete && (
        <div className="result-panel">
          <div className="result-message">
            {gameState.resultMessage || 'Раздача завершена'}
          </div>
          <button
            onClick={handleNextHand}
            disabled={loading}
            className="btn-next-hand"
          >
            {loading ? 'Загрузка...' : 'Следующая раздача'}
          </button>
        </div>
      )}

      {!gameState.handComplete && gameState.isHumanTurn && (
        <div className="action-panel">
          {gameState.recommendation && (() => {
            const parts = gameState.recommendation.split(': ')
            const action = parts[0]
            const reasoning = parts.slice(1).join(': ')
            return (
              <div className="recommendation">
                <div className="rec-header">
                  <span className="rec-label">Подсказка:</span>
                </div>
                <div className="rec-action">Действие: <strong>{action}</strong></div>
                <div className="rec-reasoning">Обоснование: {reasoning}</div>
              </div>
            )
          })()}
          <div className="action-buttons">
            <button
              onClick={() => handleAction('FOLD')}
              disabled={loading}
              className="btn-fold"
            >
              Фолд
            </button>

            {canCheck ? (
              <button
                onClick={() => handleAction('CHECK')}
                disabled={loading}
                className="btn-check"
              >
                Чек
              </button>
            ) : (
              <button
                onClick={() => handleAction('CALL')}
                disabled={loading}
                className="btn-call"
              >
                Колл ${amountToCall}
              </button>
            )}

            <div className="raise-controls">
              <input
                id="raise-amount"
                type="number"
                min={minRaise}
                max={humanPlayer?.chips || 1000}
                value={betAmount || minRaise}
                onChange={(e) => setBetAmount(Number(e.target.value))}
                className="raise-number-input"
                placeholder="Сумма"
              />
              <button
                onClick={() => handleAction('RAISE', betAmount || minRaise)}
                disabled={loading}
                className="btn-raise"
              >
                Рейз ${betAmount || minRaise}
              </button>
            </div>

            <button
              onClick={() => handleAction('ALL_IN')}
              disabled={loading}
              className="btn-allin"
            >
              Олл-ин ${humanPlayer?.chips}
            </button>
          </div>
        </div>
      )}

      {!gameState.handComplete && !gameState.isHumanTurn && (
        <div className="waiting-message">
          Ожидание хода других игроков...
        </div>
      )}
    </div>
  )
}
