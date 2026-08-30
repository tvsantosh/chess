import board.Move;
import board.Position;
import game.ChessGame;
import game.GameStatus;
import pieces.Color;
import utils.BoardPrinter;

import java.util.List;
import java.util.Scanner;

/**
 * Interactive console chess game.
 *
 * ── Move input ────────────────────────────────────────────────────────────────
 *   <from> <to>   using standard algebraic square names.
 *   Examples:   e2 e4     b1 c3     e1 g1  (kingside castling)
 *
 * ── Commands ──────────────────────────────────────────────────────────────────
 *   help      show this guide
 *   moves     list all legal moves for the current player
 *   history   print the full move history
 *   status    print current game status
 *   quit      exit
 */
public class Main {

    public static void main(String[] args) {
        ChessGame game    = new ChessGame();
        Scanner   scanner = new Scanner(System.in);

        printBanner();
        BoardPrinter.printGameState(game);

        while (true) {

            // ── Check for terminal game states ────────────────────────────────
            GameStatus status = game.gameStatus();

            if (status == GameStatus.CHECKMATE) {
                Color winner = (game.getTurn().getCurrentColor() == Color.WHITE)
                        ? Color.BLACK : Color.WHITE;
                System.out.println("  *** CHECKMATE!  " + winner + " wins. ***");
                break;
            }
            if (status == GameStatus.STALEMATE) {
                System.out.println("  *** STALEMATE — the game is a draw. ***");
                break;
            }
            if (status == GameStatus.DRAW_FIFTY_MOVE) {
                System.out.println("  *** DRAW by the 50-move rule. ***");
                break;
            }
            if (status == GameStatus.DRAW_INSUFFICIENT_MATERIAL) {
                System.out.println("  *** DRAW — insufficient material. ***");
                break;
            }

            // ── Prompt ────────────────────────────────────────────────────────
            String checkTag = (status == GameStatus.CHECK) ? "  [CHECK]" : "";
            System.out.print(game.getTurn().getCurrentColor() + "'s move" + checkTag + " > ");

            if (!scanner.hasNextLine()) break;
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.isEmpty()) continue;

            // ── Commands ──────────────────────────────────────────────────────
            switch (input) {
                case "quit": case "exit":
                    System.out.println("  Thanks for playing!");
                    scanner.close();
                    return;
                case "help":
                    printHelp();
                    continue;
                case "moves":
                    printLegalMoves(game.getLegalMoves(game.getTurn().getCurrentColor()));
                    continue;
                case "history":
                    printHistory(game.getMoveHistory());
                    continue;
                case "status":
                    BoardPrinter.printGameState(game);
                    continue;
                default:
                    break;
            }

            // ── Parse move  "e2 e4" ───────────────────────────────────────────
            String[] parts = input.split("\\s+");
            if (parts.length != 2) {
                System.out.println("  Unknown command. Type 'help' for instructions.");
                continue;
            }

            Position src  = parseSquare(parts[0]);
            Position dest = parseSquare(parts[1]);

            if (src == null || dest == null) {
                System.out.println("  Invalid square. Columns a-h, rows 1-8.  Example: e2 e4");
                continue;
            }

            // ── Execute ───────────────────────────────────────────────────────
            boolean ok = game.move(src, dest);
            if (!ok) {
                System.out.println("  Illegal move. Type 'moves' to see available moves.");
                continue;
            }

            // Print the move that was just played and the updated board
            Move last = game.getLastMove();
            System.out.println("  Played: " + last);
            BoardPrinter.printGameState(game);
        }

        // Final board
        BoardPrinter.print(game.getBoard());
        printHistory(game.getMoveHistory());
        scanner.close();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Converts "e2" → Position(1,4), returns null on bad input. */
    private static Position parseSquare(String s) {
        if (s == null || s.length() != 2) return null;
        int col = s.charAt(0) - 'a';
        int row = s.charAt(1) - '1';
        Position p = new Position(row, col);
        return p.isValid() ? p : null;
    }

    private static void printBanner() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║          JAVA CHESS  v2.0            ║");
        System.out.println("║  Castling · En Passant · Promotion   ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println("  Type 'help' for instructions.\n");
    }

    private static void printHelp() {
        System.out.println();
        System.out.println("  MOVES:    <from> <to>   e.g.  e2 e4");
        System.out.println("  CASTLING: e1 g1  (kingside)   e1 c1  (queenside)");
        System.out.println("  COLUMNS:  a-h   ROWS: 1-8");
        System.out.println();
        System.out.println("  Piece symbols (upper=White, lower=Black):");
        System.out.println("    K/k=King  Q/q=Queen  R/r=Rook");
        System.out.println("    B/b=Bishop  N/n=Knight  P/p=Pawn");
        System.out.println();
        System.out.println("  COMMANDS:");
        System.out.println("    moves    — list all legal moves");
        System.out.println("    history  — show move history");
        System.out.println("    status   — redraw board and status");
        System.out.println("    help     — show this message");
        System.out.println("    quit     — exit the game");
        System.out.println();
    }

    private static void printLegalMoves(List<Move> moves) {
        if (moves.isEmpty()) {
            System.out.println("  No legal moves available.");
            return;
        }
        System.out.println("  Legal moves (" + moves.size() + "):");
        StringBuilder line = new StringBuilder("    ");
        int count = 0;
        for (Move m : moves) {
            String entry = BoardPrinter.squareLabel(m.getSource())
                         + "-"
                         + BoardPrinter.squareLabel(m.getDestination());
            if (m.isCastling())  entry = m.toString();
            if (m.isPromotion()) entry += "=Q";
            line.append(String.format("%-9s", entry));
            if (++count % 8 == 0) {
                System.out.println(line);
                line = new StringBuilder("    ");
            }
        }
        if (count % 8 != 0) System.out.println(line);
        System.out.println();
    }

    private static void printHistory(List<Move> history) {
        if (history.isEmpty()) {
            System.out.println("  No moves played yet.");
            return;
        }
        System.out.println("  Move history:");
        for (int i = 0; i < history.size(); i++) {
            if (i % 2 == 0) System.out.print("  " + (i / 2 + 1) + ". ");
            System.out.printf("%-10s", history.get(i).toString());
            if (i % 2 == 1) System.out.println();
        }
        if (history.size() % 2 == 1) System.out.println();
        System.out.println();
    }
}
