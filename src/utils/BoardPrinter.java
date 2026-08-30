package utils;

import board.Board;
import board.Position;
import game.ChessGame;
import pieces.*;

public class BoardPrinter {

    // ANSI colour codes for terminal highlighting
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String FG_WHITE = "\u001B[97m";   // bright white — White pieces
    private static final String FG_BLACK = "\u001B[33m";   // yellow       — Black pieces
    private static final String BG_LIGHT = "\u001B[47m";   // light square
    private static final String BG_DARK = "\u001B[100m";  // dark square

    /**
     * Prints the board from White's perspective (rank 1 at the bottom).
     * Columns a–h, ranks 1–8.
     */
    public static void print(Board board) {
        System.out.println();
        System.out.println("    a   b   c   d   e   f   g   h");
        System.out.println("  +---+---+---+---+---+---+---+---+");

        for (int row = 7; row >= 0; row--) {
            System.out.print((row + 1) + " |");
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                System.out.print(" " + symbol(piece) + " |");
            }
            System.out.println(" " + (row + 1));
            System.out.println("  +---+---+---+---+---+---+---+---+");
        }

        System.out.println("    a   b   c   d   e   f   g   h");
        System.out.println();
    }

    /**
     * Prints the board with a status line above it.
     *   statusLine — e.g. "WHITE's turn", "BLACK is in CHECK"
     */
    public static void print(Board board, String statusLine) {
        System.out.println("  === " + statusLine + " ===");
        print(board);
    }

    /**
     * Full game snapshot: board + status + last move + half-move clock.
     */
    public static void printGameState(ChessGame game) {
        String status   = formatStatus(game);
        String lastMove = game.getLastMove() != null
                ? "Last move: " + game.getLastMove()
                : "Game start";
        String clock    = "50-move clock: " + game.getHalfMoveClock() + "/100";

        System.out.println("  " + lastMove + "   |   " + clock);
        print(game.getBoard(), status);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Returns a single-character symbol for a piece.
     * Upper-case = White, lower-case = Black, space = empty square.
     */
    public static String symbol(Piece piece) {
        if (piece == null) return " ";
        char c;
        if      (piece instanceof King)
        {
            c = 'K';
        }
        else if (piece instanceof Queen)
        {
            c = 'Q';
        }
        else if (piece instanceof Rook)
        {
            c = 'R';
        }
        else if (piece instanceof Bishop)
        {
            c = 'B';
        }
        else if (piece instanceof Knight)
        {
            c = 'N';
        }
        else if (piece instanceof Pawn)
        {
            c = 'P';
        }
        else
        {
            c = '?';
        }

        return piece.getColor() == pieces.Color.WHITE
                ? String.valueOf(c)
                : String.valueOf(Character.toLowerCase(c));
    }

    /** Converts a Position to algebraic notation, e.g. Position(1,4) → "e2". */
    public static String squareLabel(Position p) {
        return "" + (char)('a' + p.getCol()) + (p.getRow() + 1);
    }

    /** Formats the current GameStatus into a human-readable string. */
    private static String formatStatus(ChessGame game) {
        game.gameStatus(); // ensure fresh
        switch (game.gameStatus()) {
            case CHECK:
                return game.getTurn().getCurrentColor() + "'s turn  *** CHECK ***";
            case CHECKMATE:
                return game.getTurn().getCurrentColor() + " is CHECKMATED";
            case STALEMATE:
                return "STALEMATE — draw";
            case DRAW_FIFTY_MOVE:
                return "DRAW — 50-move rule";
            case DRAW_INSUFFICIENT_MATERIAL:
                return "DRAW — insufficient material";
            default:
                return game.getTurn().getCurrentColor() + "'s turn";
        }
    }
}
