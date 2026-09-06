# Chess REST API

The desktop chess engine is now exposed through a small HTTP/JSON API. The API uses the existing `ChessGame` and `ChessAi` classes, so move validation, check/checkmate, castling, en-passant, promotion, draw rules, and the minimax AI remain in the chess domain layer.

## Start

Run `api.ChessApiServer` from the project using Java 21.

Server:

`http://localhost:8080`

Health:

`GET /api/health`

## Endpoints

### Get current game

`GET /api/game`

Returns the current turn, game status, board pieces, move history, half-move clock, and en-passant target.

### Make a move

`POST /api/game/moves`

Request:

```json
{
  "from": "e2",
  "to": "e4"
}
```

An illegal move returns HTTP 400. A legal move returns the updated game state.

### Ask the AI to move

`POST /api/game/ai-move`

The API asks `ChessAi` for a move for the current side, applies it through `ChessGame.move()`, and returns the move plus the updated state.

### Reset game

`POST /api/game/reset`

Creates a new `ChessGame` and returns the initial state.

## Frontend example

```javascript
const API = "http://localhost:8080/api";

async function getGame() {
  const response = await fetch(`${API}/game`);
  return response.json();
}

async function makeMove(from, to) {
  const response = await fetch(`${API}/game/moves`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ from, to })
  });
  return response.json();
}

async function aiMove() {
  const response = await fetch(`${API}/game/ai-move`, { method: "POST" });
  return response.json();
}
```

## Board coordinates

The API exposes chess squares in standard notation (`a1` through `h8`). Internally the project continues to use `Position(row, col)`.

## Architecture

```text
React / Angular / JavaScript
          |
          | HTTP + JSON
          v
   ChessApiServer
          |
          v
      ChessGame
       /     \
    Board   ChessAi
              |
           Minimax
```

The API is intentionally thin: game rules stay inside the existing Java classes instead of being duplicated in the web layer.
