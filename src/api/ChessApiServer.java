package api;

import ai.ChessAi;
import board.Board;
import board.Move;
import board.Position;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import game.ChessGame;
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

/** Lightweight REST API around the existing ChessGame model. */
public class ChessApiServer {
    private static final int PORT = 8080;
    private ChessGame game = new ChessGame();
    private final ChessAi chessAi = new ChessAi();
    private final Object lock = new Object();

    public static void main(String[] args) throws Exception { new ChessApiServer().start(); }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/game", this::handleGame);
        server.createContext("/api/health", this::handleHealth);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("Chess REST API running at http://localhost:" + PORT);
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) { send(exchange, 405, "{\"error\":\"Method not allowed\"}"); return; }
        send(exchange, 200, "{\"status\":\"ok\",\"service\":\"chess-api\"}");
    }

    private void handleGame(HttpExchange exchange) throws IOException {
        addCors(exchange);
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        try {
            synchronized (lock) {
                if ("OPTIONS".equalsIgnoreCase(method)) { send(exchange, 204, ""); return; }
                if ("GET".equalsIgnoreCase(method) && "/api/game".equals(path)) { send(exchange, 200, gameJson()); return; }
                if ("POST".equalsIgnoreCase(method) && "/api/game/moves".equals(path)) { handleMove(exchange); return; }
                if ("POST".equalsIgnoreCase(method) && "/api/game/ai-move".equals(path)) { handleAiMove(exchange); return; }
                if ("POST".equalsIgnoreCase(method) && "/api/game/reset".equals(path)) {
                    game = new ChessGame();
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
        if (!validSquare(from) || !validSquare(to)) { send(exchange, 400, "{\"error\":\"from and to must be chess squares such as e2 and e4\"}"); return; }
        boolean moved = game.move(toPosition(from), toPosition(to));
        if (!moved) { send(exchange, 400, "{\"success\":false,\"error\":\"Illegal move\",\"game\":" + gameJson() + "}"); return; }
        send(exchange, 200, "{\"success\":true,\"game\":" + gameJson() + "}");
    }

    private void handleAiMove(HttpExchange exchange) throws IOException {
        Color aiColor = game.getTurn().getCurrentColor();
        Move aiMove = chessAi.findMove(game, aiColor);
        if (aiMove == null) { send(exchange, 200, "{\"success\":false,\"move\":null,\"game\":" + gameJson() + "}"); return; }
        String moveText = aiMove.toString();
        boolean moved = game.move(aiMove.getSource(), aiMove.getDestination());
        if (!moved) { send(exchange, 500, "{\"success\":false,\"error\":\"AI move could not be applied\"}"); return; }
        send(exchange, 200, "{\"success\":true,\"move\":\"" + jsonEscape(moveText) + "\",\"game\":" + gameJson() + "}");
    }

    private String gameJson() {
        Board board = game.getBoard();
        StringBuilder json = new StringBuilder("{\"turn\":\"");
        json.append(game.getTurn().getCurrentColor()).append("\",\"status\":\"").append(game.gameStatus()).append("\",");
        json.append("\"halfMoveClock\":").append(game.getHalfMoveClock()).append(",\"enPassantTarget\":");
        if (game.getEnPassantTarget() == null) json.append("null"); else json.append("\"").append(squareLabel(game.getEnPassantTarget())).append("\"");
        json.append(",\"board\":[");
        boolean first = true;
        for (int row = 0; row < 8; row++) for (int col = 0; col < 8; col++) {
            Piece piece = board.getPiece(new Position(row, col));
            if (piece == null) continue;
            if (!first) json.append(','); first = false;
            json.append("{\"square\":\"").append(squareLabel(piece.getPosition())).append("\",\"color\":\"")
                    .append(piece.getColor()).append("\",\"type\":\"").append(pieceType(piece)).append("\"}");
        }
        json.append("],\"moves\":[");
        List<Move> history = game.getMoveHistory();
        for (int i = 0; i < history.size(); i++) { if (i > 0) json.append(','); json.append("\"").append(jsonEscape(history.get(i).toString())).append("\""); }
        return json.append("]}").toString();
    }

    private static String pieceType(Piece p) {
        if (p instanceof pieces.King) return "KING"; if (p instanceof pieces.Queen) return "QUEEN";
        if (p instanceof pieces.Rook) return "ROOK"; if (p instanceof pieces.Bishop) return "BISHOP";
        if (p instanceof pieces.Knight) return "KNIGHT"; if (p instanceof pieces.Pawn) return "PAWN"; return "UNKNOWN";
    }
    private static String squareLabel(Position p) { return "" + (char)('a' + p.getCol()) + (p.getRow() + 1); }
    private static Position toPosition(String s) { return new Position(s.charAt(1) - '1', s.charAt(0) - 'a'); }
    private static boolean validSquare(String s) { return s != null && s.matches("[a-h][1-8]"); }
    private static String jsonValue(String json, String key) {
        Matcher m = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(json == null ? "" : json);
        return m.find() ? m.group(1) : null;
    }
    private static String readBody(HttpExchange e) throws IOException { try (InputStream in = e.getRequestBody()) { return new String(in.readAllBytes(), StandardCharsets.UTF_8); } }
    private static void addCors(HttpExchange e) { e.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); e.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,OPTIONS"); e.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type"); e.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8"); }
    private static void send(HttpExchange e, int status, String body) throws IOException { addCors(e); byte[] b = body.getBytes(StandardCharsets.UTF_8); e.sendResponseHeaders(status, b.length); try (OutputStream out = e.getResponseBody()) { out.write(b); } }
    private static String jsonEscape(String s) { return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r"); }
}
