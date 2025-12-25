# Покер Тренер

Симулятор Texas Hold'em для тренировки покерной стратегии. Играйте против AI-противников с разными стилями игры и получайте рекомендации в реальном времени.

## Возможности

- Texas Hold'em с 2-6 игроками
- AI-противники с разными стилями:
  - **Tight-Passive** - играет мало рук, редко рейзит
  - **Loose-Aggressive** - много рейзит, играет слабые руки
  - **Calling Station** - коллирует слишком часто
  - **Смешанный** - разные стили за одним столом
- Рекомендации по действиям в реальном времени
- Настраиваемые блайнды (10/20, 25/50, 50/100, 100/200)
- Выбор начального стека (500 - 5000 фишек)
- Анимации и премиальный дизайн

## Технологии

**Backend:**
- Java 21
- Spring Boot 3.4
- Gradle

**Frontend:**
- React 18
- TypeScript
- Vite
- Motion (анимации)

## Запуск

### Требования

- Java 21+
- Node.js 18+
- npm

### Backend

```bash
# Запуск сервера (порт 8080)
./gradlew :backend:bootRun
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Откройте http://localhost:5173

### Или всё вместе

```bash
# Терминал 1 - Backend
./gradlew :backend:bootRun

# Терминал 2 - Frontend
cd frontend && npm run dev
```

## Сборка

```bash
# Собрать весь проект
./gradlew build

# Собрать только backend
./gradlew :backend:build

# Собрать frontend для production
cd frontend && npm run build
```

## API

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/api/game/create` | Создать игру |
| POST | `/api/game/{id}/start` | Начать раздачу |
| GET | `/api/game/{id}/state` | Получить состояние |
| POST | `/api/game/{id}/action` | Сделать действие |
| GET | `/api/game/health` | Health check |

## Структура проекта

```
poker-sim/
├── backend/                 # Spring Boot API
│   └── src/main/java/poker/
│       ├── ai/              # AI стратегии
│       ├── analytics/       # Анализ решений
│       ├── engine/          # Игровой движок
│       ├── evaluation/      # Оценка рук
│       ├── model/           # Модели данных
│       └── web/             # REST контроллеры
├── frontend/                # React приложение
│   └── src/
│       ├── components/      # UI компоненты
│       ├── api.ts           # API клиент
│       └── types.ts         # TypeScript типы
└── gradle/                  # Gradle wrapper
```

## Скриншоты

### Настройка игры
Выбор количества игроков, стека, блайндов и стиля AI.
<img width="687" height="1239" alt="image" src="https://github.com/user-attachments/assets/9a104bfc-bb67-48bb-96cc-c32fe55811b3" />


### Покерный стол
Овальный стол с игроками, картами и панелью действий.
<img width="1198" height="1147" alt="image" src="https://github.com/user-attachments/assets/c6dbc187-0172-4cb2-83cf-678c47bae285" />

### Рекомендации
Подсказки по оптимальным действиям с учётом позиции.

## Лицензия

MIT

