package ui;

import ai.ChessAi;
import board.Move;
import board.Position;
import game.ChessGame;
import game.GameStatus;
import pieces.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class ChessApplication extends Application {

    // ── Game state ────────────────────────────────────────────────────────────
    private ChessGame    game;
    private ChessAi      chessAi;
    private Position     selectedPosition;
    private List<Move>   legalMovesCache = new ArrayList<>();

    // ── Mode: true = Human vs AI,  false = Human vs Human ────────────────────
    private boolean      vsAI = false;

    // ── Prevent clicks while AI is thinking ───────────────────────────────────
    private boolean      aiThinking = false;

    // ── UI components ─────────────────────────────────────────────────────────
    private GridPane         chessBoard;
    private Label            turnLabel;
    private Label            statusLabel;
    private Label            moveHistoryLabel;
    private final Button[][] boardButtons = new Button[8][8];

    // ── Square colors ─────────────────────────────────────────────────────────
    private static final String LIGHT_COLOR    = "#F0D9B5";
    private static final String DARK_COLOR     = "#B58863";
    private static final String SELECTED_COLOR = "#F6F669";
    private static final String LEGAL_COLOR    = "#90EE90";
    private static final String CAPTURE_COLOR  = "#FF6B6B";

    // ── Coordinate conversion ─────────────────────────────────────────────────
    // Game row 0 = White's back rank (bottom). UI row 0 = top of screen.
    private static int toGameRow(int uiRow) { return 7 - uiRow; }

    // =========================================================================
    // START
    // =========================================================================

    @Override
    public void start(Stage stage) {
        game    = new ChessGame();
        chessAi = new ChessAi();

        // ── Top: title + turn label ───────────────────────────────────────────
        Label title = new Label("JAVA CHESS");
        title.setFont(Font.font("Arial", 28));

        turnLabel = new Label("WHITE's Turn");
        turnLabel.setFont(Font.font("Arial", 18));

        VBox top = new VBox(6, title, turnLabel);
        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(0, 0, 10, 0));

        // ── Board ─────────────────────────────────────────────────────────────
        chessBoard = new GridPane();
        chessBoard.setAlignment(Pos.CENTER);
        buildBoard();

        // ── Bottom controls ───────────────────────────────────────────────────
        statusLabel = new Label("Select a mode and press New Game");
        statusLabel.setFont(Font.font("Arial", 14));

        moveHistoryLabel = new Label("Move history: —");
        moveHistoryLabel.setFont(Font.font("Arial", 12));

        // Mode toggle: Human vs Human  |  Human vs AI
        ToggleGroup modeGroup  = new ToggleGroup();

        ToggleButton hvhButton = new ToggleButton("👥  Human vs Human");
        hvhButton.setFont(Font.font("Arial", 13));
        hvhButton.setToggleGroup(modeGroup);
        hvhButton.setSelected(true);   // default
        hvhButton.setOnAction(e -> {
            vsAI = false;
            statusLabel.setText("Mode: Human vs Human — press New Game");
        });

        ToggleButton hvaiButton = new ToggleButton("🤖  Human vs AI");
        hvaiButton.setFont(Font.font("Arial", 13));
        hvaiButton.setToggleGroup(modeGroup);
        hvaiButton.setOnAction(e -> {
            vsAI = true;
            statusLabel.setText("Mode: Human vs AI (you play White) — press New Game");
        });

        // Style the toggle buttons
        String toggleBase = "-fx-background-radius:5; -fx-border-radius:5;";
        hvhButton.setStyle(toggleBase);
        hvaiButton.setStyle(toggleBase);

        HBox modeBox = new HBox(8, hvhButton, hvaiButton);
        modeBox.setAlignment(Pos.CENTER);

        Button newGameButton = new Button("New Game");
        newGameButton.setFont(Font.font("Arial", 14));
        newGameButton.setOnAction(e -> resetGame());

        VBox bottom = new VBox(8, modeBox, newGameButton, statusLabel, moveHistoryLabel);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(10, 0, 6, 0));

        // ── Root layout ───────────────────────────────────────────────────────
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setTop(top);
        root.setCenter(chessBoard);
        root.setBottom(bottom);

        stage.setTitle("Java Chess");
        stage.setScene(new Scene(root, 760, 900));
        stage.setResizable(false);
        stage.show();

        refreshBoard();
    }

    // =========================================================================
    // BUILD BOARD
    // =========================================================================

    private void buildBoard() {
        chessBoard.getChildren().clear();

        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                boardButtons[r][c] = null;

        // Rank labels  8 → 1
        for (int uiRow = 0; uiRow < 8; uiRow++) {
            Label rank = new Label(" " + (8 - uiRow) + " ");
            rank.setFont(Font.font("Arial", 14));
            rank.setMinWidth(25);
            rank.setAlignment(Pos.CENTER);
            chessBoard.add(rank, 0, uiRow);
        }

        // Squares
        for (int uiRow = 0; uiRow < 8; uiRow++) {
            for (int col = 0; col < 8; col++) {
                Button sq = new Button();
                sq.setPrefSize(80, 80);
                sq.setMinSize(80, 80);
                sq.setMaxSize(80, 80);
                sq.setFocusTraversable(false);
                applyBaseColor(sq, uiRow, col);

                final int r = uiRow, c = col;
                sq.setOnAction(e -> handleSquareClick(r, c));

                boardButtons[uiRow][col] = sq;
                chessBoard.add(sq, col + 1, uiRow);
            }
        }

        // File labels  a → h
        String[] files = {"a","b","c","d","e","f","g","h"};
        for (int col = 0; col < 8; col++) {
            Label file = new Label(" " + files[col] + " ");
            file.setFont(Font.font("Arial", 14));
            file.setMinWidth(80);
            file.setAlignment(Pos.CENTER);
            chessBoard.add(file, col + 1, 8);
        }
    }

    // =========================================================================
    // PIECE IMAGES
    // =========================================================================

    private ImageView getPieceImage(Piece piece) {
        if (piece == null) return null;

        boolean white  = piece.getColor() == Color.WHITE;
        String  folder = white ? "/resources/whitepieces/" : "/resources/blackpieces/";
        String  file;

        if      (piece instanceof King)   file = white ? "white_king.png"   : "black_king.png";
        else if (piece instanceof Queen)  file = white ? "white_queen.png"  : "black_queen.png";
        else if (piece instanceof Rook)   file = white ? "white_rook.png"   : "black_rook.png";
        else if (piece instanceof Bishop) file = white ? "white_bishop.png" : "black_bishop.png";
        else if (piece instanceof Knight) file = white ? "white_knight.png" : "black_knight.png";
        else if (piece instanceof Pawn)   file = white ? "white_pawn.png"   : "black_pawn.png";
        else return null;

        var stream = getClass().getResourceAsStream(folder + file);
        if (stream == null) {
            System.err.println("Image not found: " + folder + file);
            return null;
        }

        ImageView iv = new ImageView(new Image(stream));
        iv.setFitWidth(62);
        iv.setFitHeight(62);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        return iv;
    }

    // =========================================================================
    // CLICK HANDLER
    // =========================================================================

    private void handleSquareClick(int uiRow, int col) {
        // Block input while AI is thinking or game is over
        if (aiThinking) return;
        GameStatus status = game.gameStatus();
        if (status == GameStatus.CHECKMATE
                || status == GameStatus.STALEMATE
                || status == GameStatus.DRAW_FIFTY_MOVE
                || status == GameStatus.DRAW_INSUFFICIENT_MATERIAL) return;

        // In AI mode, only allow White (human) to click
        if (vsAI && game.getTurn().getCurrentColor() == Color.BLACK) return;

        int      gameRow = toGameRow(uiRow);
        Position clicked = new Position(gameRow, col);
        Piece    piece   = game.getBoard().getPiece(clicked);

        // ── Nothing selected yet ──────────────────────────────────────────────
        if (selectedPosition == null) {
            if (piece == null) {
                statusLabel.setText("Select a piece first.");
                return;
            }
            if (piece.getColor() != game.getTurn().getCurrentColor()) {
                statusLabel.setText("It's " + game.getTurn().getCurrentColor() + "'s turn.");
                return;
            }
            selectedPosition = clicked;
            refreshBoard();
            statusLabel.setText("Selected " + getPieceName(piece) + " at " + squareLabel(clicked));
            return;
        }

        // ── Click same square → deselect ──────────────────────────────────────
        if (samePosition(selectedPosition, clicked)) {
            selectedPosition = null;
            refreshBoard();
            statusLabel.setText("Piece deselected.");
            return;
        }

        // ── Try to move ───────────────────────────────────────────────────────
        boolean moved = game.move(selectedPosition, clicked);
        if (moved) {
            selectedPosition = null;
            refreshBoard();
            Move last = game.getLastMove();
            if (last != null) statusLabel.setText("You played: " + last);
            updateMoveHistoryLabel();

            // Check terminal state before triggering AI
            if (isGameOver()) {
                checkTerminalState();
                return;
            }

            // In AI mode, trigger AI response on a background thread
            if (vsAI) {
                triggerAIMove();
            }

        } else {
            // Re-select if the click landed on another own piece
            if (piece != null && piece.getColor() == game.getTurn().getCurrentColor()) {
                selectedPosition = clicked;
                refreshBoard();
                statusLabel.setText("Selected " + getPieceName(piece) + " at " + squareLabel(clicked));
            } else {
                statusLabel.setText("Illegal move.");
            }
        }
    }

    // =========================================================================
    // AI MOVE  — runs on a background thread, updates UI on FX thread
    // =========================================================================

    private void triggerAIMove() {
        aiThinking = true;
        setBoardDisabled(true);
        turnLabel.setText("AI is thinking…");

        Thread aiThread = new Thread(() -> {
            // Compute best move off the FX thread so UI stays responsive
            Move aiMove = chessAi.findMove(game, Color.BLACK);

            // Return to FX thread to apply the move and refresh UI
            Platform.runLater(() -> {
                aiThinking = false;
                setBoardDisabled(false);

                if (aiMove == null) {
                    // No legal moves for AI — terminal state
                    checkTerminalState();
                    return;
                }

                game.move(aiMove.getSource(), aiMove.getDestination());
                refreshBoard();
                updateMoveHistoryLabel();

                Move last = game.getLastMove();
                if (last != null) statusLabel.setText("AI played: " + last);

                checkTerminalState();
            });
        });

        aiThread.setDaemon(true);   // don't block JVM shutdown
        aiThread.setName("chess-ai");
        aiThread.start();
    }

    // =========================================================================
    // REFRESH BOARD
    // =========================================================================

    private void refreshBoard() {
        legalMovesCache = (selectedPosition != null)
                ? game.getLegalMoves(game.getTurn().getCurrentColor())
                : new ArrayList<>();

        for (int uiRow = 0; uiRow < 8; uiRow++) {
            for (int col = 0; col < 8; col++) {
                int    gameRow = toGameRow(uiRow);
                Button sq      = boardButtons[uiRow][col];
                if (sq == null) continue;

                applyBaseColor(sq, uiRow, col);

                Piece piece = game.getBoard().getPiece(new Position(gameRow, col));
                sq.setText("");
                sq.setGraphic(piece != null ? getPieceImage(piece) : null);

                // Selected square highlight
                if (selectedPosition != null
                        && gameRow == selectedPosition.getRow()
                        && col    == selectedPosition.getCol()) {
                    sq.setStyle("-fx-background-color:" + SELECTED_COLOR
                            + ";-fx-border-color:#555;-fx-border-width:3;");
                    continue;
                }

                // Legal destination highlight
                if (isLegalDestination(gameRow, col)) {
                    sq.setStyle("-fx-background-color:"
                            + (piece != null ? CAPTURE_COLOR : LEGAL_COLOR) + ";");
                }
            }
        }

        updateTurnLabel();
    }

    // =========================================================================
    // TERMINAL STATE
    // =========================================================================

    private boolean isGameOver() {
        GameStatus s = game.gameStatus();
        return s == GameStatus.CHECKMATE
                || s == GameStatus.STALEMATE
                || s == GameStatus.DRAW_FIFTY_MOVE
                || s == GameStatus.DRAW_INSUFFICIENT_MATERIAL;
    }

    private void checkTerminalState() {
        GameStatus status = game.gameStatus();
        switch (status) {
            case CHECKMATE -> {
                Color winner = game.getTurn().getCurrentColor() == Color.WHITE
                        ? Color.BLACK : Color.WHITE;
                statusLabel.setText("CHECKMATE!  " + winner + " wins! 🎉");
                turnLabel.setText("Game Over");
            }
            case STALEMATE -> {
                statusLabel.setText("STALEMATE — Draw!");
                turnLabel.setText("Game Over");
            }
            case DRAW_FIFTY_MOVE -> {
                statusLabel.setText("DRAW — 50-move rule");
                turnLabel.setText("Game Over");
            }
            case DRAW_INSUFFICIENT_MATERIAL -> {
                statusLabel.setText("DRAW — Insufficient material");
                turnLabel.setText("Game Over");
            }
            case CHECK ->
                statusLabel.setText(game.getTurn().getCurrentColor() + " is in CHECK!");
            default -> {}
        }
    }

    // =========================================================================
    // TURN LABEL
    // =========================================================================

    private void updateTurnLabel() {
        if (aiThinking) return;   // label already set to "AI is thinking…"
        GameStatus status = game.gameStatus();
        switch (status) {
            case CHECK ->
                turnLabel.setText(game.getTurn().getCurrentColor() + "'s Turn  *** CHECK ***");
            case CHECKMATE, STALEMATE, DRAW_FIFTY_MOVE, DRAW_INSUFFICIENT_MATERIAL ->
                turnLabel.setText("Game Over");
            default -> {
                Color c = game.getTurn().getCurrentColor();
                String who = (vsAI && c == Color.BLACK) ? "AI (BLACK)" : c + "'s Turn";
                turnLabel.setText(who);
            }
        }
    }

    // =========================================================================
    // RESET
    // =========================================================================

    private void resetGame() {
        // If AI thread is running, let it finish then ignore its result
        aiThinking = false;
        game             = new ChessGame();
        selectedPosition = null;
        legalMovesCache  = new ArrayList<>();
        moveHistoryLabel.setText("Move history: —");
        buildBoard();
        refreshBoard();
        String mode = vsAI ? "Human (White) vs AI (Black)" : "Human vs Human";
        statusLabel.setText("New game — " + mode + "  |  " + legalMoveCount() + " legal moves");
    }

    // =========================================================================
    // MOVE HISTORY
    // =========================================================================

    private void updateMoveHistoryLabel() {
        List<Move> history = game.getMoveHistory();
        if (history == null || history.isEmpty()) {
            moveHistoryLabel.setText("Move history: —");
            return;
        }
        int from = Math.max(0, history.size() - 6);
        StringBuilder sb = new StringBuilder("Moves: ");
        for (int i = from; i < history.size(); i++) {
            if (i % 2 == 0) sb.append(i / 2 + 1).append(". ");
            sb.append(history.get(i)).append("  ");
        }
        moveHistoryLabel.setText(sb.toString().trim());
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    /** Enables or disables all board squares (used while AI is thinking). */
    private void setBoardDisabled(boolean disabled) {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                if (boardButtons[r][c] != null)
                    boardButtons[r][c].setDisable(disabled);
    }

    private boolean isLegalDestination(int gameRow, int col) {
        if (selectedPosition == null) return false;
        for (Move m : legalMovesCache) {
            if (m.getSource().getRow()      == selectedPosition.getRow()
             && m.getSource().getCol()      == selectedPosition.getCol()
             && m.getDestination().getRow() == gameRow
             && m.getDestination().getCol() == col) return true;
        }
        return false;
    }

    private boolean samePosition(Position a, Position b) {
        return a.getRow() == b.getRow() && a.getCol() == b.getCol();
    }

    private void applyBaseColor(Button sq, int uiRow, int col) {
        String color = (uiRow + col) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR;
        sq.setStyle("-fx-background-color:" + color + ";");
    }

    private String getPieceName(Piece piece) {
        if (piece instanceof King)   return "King";
        if (piece instanceof Queen)  return "Queen";
        if (piece instanceof Rook)   return "Rook";
        if (piece instanceof Bishop) return "Bishop";
        if (piece instanceof Knight) return "Knight";
        if (piece instanceof Pawn)   return "Pawn";
        return "Piece";
    }

    private String squareLabel(Position p) {
        return "" + (char)('a' + p.getCol()) + (p.getRow() + 1);
    }

    private int legalMoveCount() {
        return game.getLegalMoves(game.getTurn().getCurrentColor()).size();
    }

    public static void main(String[] args) { launch(args); }
}
