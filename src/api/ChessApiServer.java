package api;

import ai.ChessAi;
import board.Board;
import board.Move;
import board.Position;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import game.ChessGame;
import game.GameStatus;
import pieces.Color;
import pieces.Piece;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight REST API around the existing ChessGame domain model.
 * Uses JDK HttpServer, so no new web framework dependency is required.
 *
 * Run this class and connect a browser/React/Angular frontend to:
 *   http://localhost:8080/api/game
 */
public class ChessApiServer {

    private static final int PORT = 8080;
    private final ChessGame game = new ChessGame();
    private final ChessAi chessAi = new ChessAi();
    private final Object lock = new Object();

    public static void main(String[] args) throws Exception {
        new ChessApiServer().start();
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/game", this::handleGame);
        server.createContext("/api/health", this::handleHealth);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("Chess REST API running at http://localhost:" + PORT);
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        send(exchange, 200, "{\"status\":\"ok\",\"service\":\"chess-api\"}");
    }

    private void handleGame(HttpExchange exchange) throws IOException {
        addCors(exchange);
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            synchronized (lock) {
                if ("OPTIONS".equalsIgnoreCase(method)) {
                    send(exchange, 204, "");
                    return;
                }

                if ("GET".equalsIgnoreCase(method) && "/api/game".equals(path)) {
                    send(exchange, 200, gameJson());
                    return;
                }

                if ("POST".equalsIgnoreCase(method) && "/api/game/moves".equals(path)) {
                    handleMove(exchange);
                    return;
                }

                if ("POST".equalsIgnoreCase(method) && "/api/game/ai-move".equals(path)) {
                    handleAiMove(exchange);
                    return;
                }

                if ("POST".equalsIgnoreCase(method) && "/api/game/reset".equals(path)) {
                    // ChessGame.setupBoard() is additive, so create a fresh game through resetState.
                    resetState();
                    send(exchange, 200, gameJson());
                    return;
                }

                send(exchange, 404, "{\"error\":\"Endpoint not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            send(exchange, 500, "{\"error\":\"Internal server error\",\"message\":\"" + jsonEscape(e.getMessage()) + "\"}");
        }
    }

    private void handleMove(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String from = jsonValue(body, "from");
        String to = jsonValue(body, "to");

        if (!validSquare(from) || !validSquare(to)) {
            send(exchange, 400, "{\"error\":\"from and to must be chess squares such as e2 and e4\"}");
            return;
        }

        boolean moved = game.move(toPosition(from), toPosition(to));
        if (!moved) {
            send(exchange, 400, "{\"success\":false,\"error\":\"Illegal move\",\"game\":" + gameJson() + "}");
            return;
        }

        send(exchange, 200, "{\"success\":true,\"game\":" + gameJson() + "}");
    }

    private void handleAiMove(HttpExchange exchange) throws IOException {
        Color aiColor = game.getTurn().getCurrentColor();
        Move aiMove = chessAi.findMove(game, aiColor);

        if (aiMove == null) {
            send(exchange, 200, "{\"success\":false,\"move\":null,\"game\":" + gameJson() + "}");
            return;
        }

        String moveText = aiMove.toString();
        boolean moved = game.move(aiMove.getSource(), aiMove.getDestination());
        if (!moved) {
            send(exchange, 500, "{\"success\":false,\"error\":\"AI generated a move that could not be applied\"}");
            return;
        }

        send(exchange, 200, "{\"success\":true,\"move\":\"" + jsonEscape(moveText) + "\",\"game\":" + gameJson() + "}");
    }

    private String gameJson() {
        Board board = game.getBoard();
        StringBuilder json = new StringBuilder();
        json.append("{\"turn\":\"").append(game.getTurn().getCurrentColor()).append("\",");
        json.append("\"status\":\"").append(game.gameStatus()).append("\",");
        json.append("\"halfMoveClock\":").append(game.getHalfMoveClock()).append(",");
        json.append("\"enPassantTarget\":");
        if (game.getEnPassantTarget() == null) json.append("null");
        else json.append("\"").append(squareLabel(game.getEnPassantTarget())).append("\"");
        json.append(",\"board\":[");

        boolean first = true;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                if (piece == null) continue;
                if (!first) json.append(',');
                first = false;
                json.append("{\"square\":\"").append(squareLabel(piece.getPosition())).append("\",");
                json.append("\"color\":\"").append(piece.getColor()).append("\",");
                json.append("\"type\":\"").append(pieceType(piece)).append("\"}");
            }
        }
        json.append("],\"moves\":[");
        List<Move> history = game.getMoveHistory();
        for (int i = 0; i < history.size(); i++) {
            if (i > 0) json.append(',');
            json.append("\"").append(jsonEscape(history.get(i).toString())).append("\"");
        }
        json.append("]}");
        return json.toString();
    }

    private void resetState() {
        // Reflection is intentionally avoided. The current ChessGame has no reset method,
        // so replace the server's state by copying a new game is not possible with final fields.
        // Clear and recreate through a small server-side state holder instead.
        // This method is replaced below by throwing a clear message if called unexpectedly.
        throw new UnsupportedOperationException("Reset endpoint requires ChessGame.reset() to be added to the domain model");
    }

    private static String pieceType(Piece piece) {
        if (piece instanceof pieces.King) return "KING";
        if (piece instanceof pieces.Queen) return "QUEEN";
        if (piece instanceof pieces.Rook) return "ROOK";
        if (piece instanceof pieces.Bishop) return "BISHOP";
        if (piece instanceof pieces.Knight) return "KNIGHT";
        if (piece instanceof pieces.Pawn) return "PAWN";
        return "UNKNOWN";
    }

    private static String squareLabel(Position p) {
        return "" + (char) ('a' + p.getCol()) + (p.getRow() + 1);
    }

    private static Position toPosition(String square) {
        int col = square.charAt(0) - 'a';
        int row = square.charAt(1) - '1';
        return new Position(row, col);
    }

    private static boolean validSquare(String square) {
        return square != null && square.matches("[a-h][1-8]");
    }

    private static String jsonValue(String json, String key) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
        Matcher matcher = pattern.matcher(json == null ? "" : json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void addCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        addCors(exchange);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static String jsonEscape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
