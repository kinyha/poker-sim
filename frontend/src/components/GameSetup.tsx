import { useState, useEffect } from 'react'
import { createGame, startHand } from '../api'
import { GameState } from '../types'
import './GameSetup.css'

interface Props {
  onGameStart: (sessionId: string, state: GameState) => void
}

export function GameSetup({ onGameStart }: Props) {
  const [playerCount, setPlayerCount] = useState(6)
  const [humanPosition, setHumanPosition] = useState(0)
  const [startingChips, setStartingChips] = useState(1000)
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
        smallBlind: 10,
        bigBlind: 20,
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
    <div className="game-setup">
      <h1>Poker Trainer</h1>
      <p className="subtitle">Texas Hold'em</p>

      <div className="setup-form">
        <div className="form-group">
          <label>Количество игроков</label>
          <select
            value={playerCount}
            onChange={(e) => setPlayerCount(Number(e.target.value))}
          >
            {[2, 3, 4, 5, 6, 7, 8, 9].map((n) => (
              <option key={n} value={n}>{n}</option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label>Ваша позиция</label>
          <select
            value={humanPosition}
            onChange={(e) => setHumanPosition(Number(e.target.value))}
          >
            {Array.from({ length: playerCount }, (_, i) => (
              <option key={i} value={i}>Место {i + 1}</option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label>Начальный стек</label>
          <select
            value={startingChips}
            onChange={(e) => setStartingChips(Number(e.target.value))}
          >
            <option value={500}>500</option>
            <option value={1000}>1000</option>
            <option value={2000}>2000</option>
            <option value={5000}>5000</option>
          </select>
        </div>

        <div className="form-group">
          <label>Тип соперников</label>
          <select
            value={aiType}
            onChange={(e) => setAiType(e.target.value)}
          >
            <option value="mixed">Смешанный (разные типы)</option>
            <option value="calling_station">Calling Station (коллирует всё)</option>
            <option value="tight_passive">Тайтовый пассивный (играет мало)</option>
            <option value="loose_aggressive">Луз-агрессивный (много рейзит)</option>
          </select>
          <p className="hint">
            {aiType === 'calling_station' && 'Ошибка: коллирует слишком часто, не фолдит слабые руки'}
            {aiType === 'tight_passive' && 'Ошибка: играет очень мало рук, боится рейзить'}
            {aiType === 'loose_aggressive' && 'Ошибка: рейзит слишком много, играет слабые руки'}
            {aiType === 'mixed' && 'Разные типы ботов за столом'}
          </p>
        </div>

        <button
          className="start-button"
          onClick={handleStart}
          disabled={loading}
        >
          {loading ? 'Загрузка...' : 'Начать игру'}
        </button>
      </div>
    </div>
  )
}
