package game;

import board.Board;
import board.Move;
import board.Position;
import pieces.*;
import player.Player;

import java.util.ArrayList;
import java.util.List;

public class ChessGame {

    // ── State ─────────────────────────────────────────────────────────────────
    private final Board  board;
    private final Turn   turn;
    private final Player whitePlayer;
    private final Player blackPlayer;

    private final List<Move> moveHistory = new ArrayList<>();

    /**
     * Half-move clock for the fifty-move rule.
     * Increments every half-move; resets to 0 on any pawn push or capture.
     * Draw is declared when this reaches 100 (= 50 full moves).
     */
    private int halfMoveClock = 0;

    /**
     * The square over which a pawn just jumped (en-passant target square).
     * Null if the last move was not a two-square pawn push.
     * Only valid for exactly one half-move after the push.
     */
    private Position enPassantTarget = null;

    public ChessGame() {
        board       = new Board();
        turn        = new Turn();
        whitePlayer = new Player(Color.WHITE);
        blackPlayer = new Player(Color.BLACK);
        setupBoard();
    }

    public Board getBoard()
    {
        return board;
    }
    public Turn getTurn()
    {
        return turn;
    }
    public List<Move>  getMoveHistory()
    {
        return moveHistory;
    }
    public Move getLastMove()
    {
        return moveHistory.isEmpty() ? null : moveHistory.get(moveHistory.size() - 1);
    }
    public Position getEnPassantTarget()
    {
        return enPassantTarget;
    }
    public int getHalfMoveClock()
    {
        return halfMoveClock;
    }

    // ── Board setup ─────
    public void setupBoard() {
        // =========================
        // White Pieces----TOP
        // =========================

        //ROOK(ELEPHANT-LEFTSIDE)
        Position pos = new Position(0, 0);
        Rook whiteRook1 = new Rook(pos, Color.WHITE);
        board.placePiece(pos, whiteRook1);

        //KNIGHT(HORSE-LEFTSIDE)
        pos = new Position(0, 1);
        Knight whiteKnight1 = new Knight(pos, Color.WHITE);
        board.placePiece(pos, whiteKnight1);

        //BISHOP(CAMEL-LEFTSIDE)
        pos = new Position(0, 2);
        Bishop whiteBishop1 = new Bishop(pos, Color.WHITE);
        board.placePiece(pos, whiteBishop1);

        //QUEEN********************************************
        pos = new Position(0, 3);
        Queen whiteQueen = new Queen(pos, Color.WHITE);
        board.placePiece(pos, whiteQueen);

        //KING*********************************************
        pos = new Position(0, 4);
        King whiteKing = new King(pos, Color.WHITE);
        board.placePiece(pos, whiteKing);

        //BISHOP(CAMEL-RIGHTSIDE)
        pos = new Position(0, 5);
        Bishop whiteBishop2 = new Bishop(pos, Color.WHITE);
        board.placePiece(pos, whiteBishop2);

        //KNIGHT(HORSE-RIGHTSIDE)
        pos = new Position(0, 6);
        Knight whiteKnight2 = new Knight(pos, Color.WHITE);
        board.placePiece(pos, whiteKnight2);

        //ROOK(ELEPHANT-RIGHTSIDE)
        pos = new Position(0, 7);
        Rook whiteRook2 = new Rook(pos, Color.WHITE);
        board.placePiece(pos, whiteRook2);


        //PAWN(SOLDIER -WHITE)
        for(int i=0;i<8;i++)
        {
            pos=new Position( 1,i );
            Pawn pawn=new Pawn( pos,Color.WHITE );
            board.placePiece( pos,pawn );
        }


        // =========================
        // Black Pieces---DOWN
        // =========================

        //ROOK(ELEPHANT-LEFTSIDE)
        pos = new Position(7, 0);
        Rook blackRook1 = new Rook(pos, Color.BLACK);
        board.placePiece(pos, blackRook1);

        //KNIGHT(HORSE-LEFTSIDE)
        pos = new Position(7, 1);
        Knight blackKnight1 = new Knight(pos, Color.BLACK);
        board.placePiece(pos, blackKnight1);

        //BISHOP(CAMEL-LEFTSIDE)
        pos = new Position(7, 2);
        Bishop blackBishop1 = new Bishop(pos, Color.BLACK);
        board.placePiece(pos, blackBishop1);

        //QUEEN***************************************************
        pos = new Position(7, 3);
        Queen blackQueen = new Queen(pos, Color.BLACK);
        board.placePiece(pos, blackQueen);

        //KING****************************************************
        pos = new Position(7, 4);
        King blackKing = new King(pos, Color.BLACK);
        board.placePiece(pos, blackKing);

        //BISHOP(CAMEL-RIGHTSIDE)
        pos = new Position(7, 5);
        Bishop blackBishop2 = new Bishop(pos, Color.BLACK);
        board.placePiece(pos, blackBishop2);

        //KNIGHT(HORSE-RIGHTSIDE)
        pos = new Position(7, 6);
        Knight blackKnight2 = new Knight(pos, Color.BLACK);
        board.placePiece(pos, blackKnight2);

        //ROOK(ELEPHANT-RIGHTSIDE)
        pos = new Position(7, 7);
        Rook blackRook2 = new Rook(pos, Color.BLACK);
        board.placePiece(pos, blackRook2);

        //PAWN(SOLDIER -BLACK)
        for(int i=0;i<8;i++)
        {
            pos=new Position( 6,i );
            Pawn pan=new Pawn( pos,Color.BLACK );
            board.placePiece( pos,pan );

        }
    }

    // =========================================================================
    // PUBLIC MOVE API
    // =========================================================================

    /**
     * Attempts to execute a move from src to dest for the current player.
     * Handles all special moves: en passant, castling, pawn promotion.
     * Returns true on success, false if the move is illegal.
     */
    public boolean move(Position src, Position dest) {
        if (!src.isValid() || !dest.isValid())
        {
            return false;
        }

        Piece piece     = board.getPiece(src);
        Piece destPiece = board.getPiece(dest);

        if (piece == null)
        {
            return false;
        }
        if (piece.getColor() != turn.getCurrentColor())
        {
            return false;
        }
        if (destPiece != null && destPiece.getColor() == piece.getColor())
        {
            return false;
        }

        // ── Castling ────
        if (piece instanceof King && Math.abs(dest.getCol() - src.getCol()) == 2) {
            return tryCastle(src, dest, (King) piece);
        }

        // ── Geometry check (non-castling) ─────────────────────────────────────
        if (!piece.isValidMove(dest))
        {
            return false;
        }

        // ── En passant ─────────
        boolean isEnPassant = false;
        Piece   epCaptured  = null;
        Position epSquare   = null;

        if (piece instanceof Pawn
                && enPassantTarget != null
                && dest.getRow() == enPassantTarget.getRow()
                && dest.getCol() == enPassantTarget.getCol()
                && Math.abs(dest.getCol() - src.getCol()) == 1)
        {
            // The pawn being captured is directly behind the en-passant target
            int capturedRow = src.getRow();   // same row as the moving pawn
            epSquare  = new Position(capturedRow, dest.getCol());
            epCaptured = board.getPiece(epSquare);
            if (epCaptured != null) isEnPassant = true;
        }

        // ── Normal pawn board rules (only when NOT en passant) ────────────────
        if (piece instanceof Pawn && !isEnPassant) {
            int crow = src.getRow(), ccol = src.getCol();
            int drow = dest.getRow(), dcol = dest.getCol();

            if (dcol == ccol) {
                if (destPiece != null) return false;
                if (Math.abs(drow - crow) == 2) {
                    int mid = (crow + drow) / 2;
                    if (board.getPiece(new Position(mid, ccol)) != null) return false;
                }
            } else {
                // Diagonal — must capture a real enemy piece on dest
                if (destPiece == null) return false;
            }
        }

        // ── Sliding piece path check ──────────────────────────────────────────
        if (piece instanceof Rook || piece instanceof Bishop || piece instanceof Queen) {
            if (!isPathClear(src, dest)) return false;
        }

        // ── Build move record ─────────────────────────────────────────────────
        Move move = new Move(src, dest, piece, destPiece);
        move.setHalfMoveClockBefore(halfMoveClock);


        if (piece instanceof King) {
            move.setPieceHadMovedBefore(((King) piece).hasMoved());
        }

        if (piece instanceof Rook) {
            move.setPieceHadMovedBefore(((Rook) piece).hasMoved());
        }


        // ── Apply move to board ───────────────────────────────────────────────
        if (isEnPassant) {
            board.removePiece(epSquare);         // remove captured pawn from its square
            board.movePiece(src, dest);
            move.markEnPassant(epCaptured, epSquare);
        } else {
            board.movePiece(src, dest);
        }

        // ── Self-check test — undo and reject if king is exposed ──────────────
        if (isKingInCheck(piece.getColor())) {
            undoMove(move);
            return false;
        }

        // ── Pawn promotion (auto-queen) ───────────────────────────────────────
        if (piece instanceof Pawn) {
            int promotionRow = (piece.getColor() == Color.WHITE) ? 7 : 0;
            if (dest.getRow() == promotionRow) {
                Queen promoted = new Queen(dest, piece.getColor());
                board.placePiece(dest, promoted);
                move.setPromotedTo(promoted);
            }
        }

        // ── Mark pieces as moved (castling eligibility) ───────────────────────
        if (piece instanceof King) ((King) piece).setHasMoved(true);
        if (piece instanceof Rook) ((Rook) piece).setHasMoved(true);

        // ── Update en-passant target for next move ────────────────────────────
        if (piece instanceof Pawn && Math.abs(dest.getRow() - src.getRow()) == 2) {
            // Record the square the pawn skipped over
            int epRow = (src.getRow() + dest.getRow()) / 2;
            enPassantTarget = new Position(epRow, src.getCol());
        } else {
            enPassantTarget = null;
        }

        // ── Update half-move clock ────────────────────────────────────────────
        boolean isCapture = (destPiece != null) || isEnPassant;
        halfMoveClock = (isCapture || piece instanceof Pawn) ? 0 : halfMoveClock + 1;

        moveHistory.add(move);
        turn.switchTurn();
        return true;
    }

    // ── Castling helper ───────────────────────────────────────────────────────
    private boolean tryCastle(Position kingSrc, Position kingDest, King king) {
        // King must not have moved
        if (king.hasMoved())
        {
            return false;
        }

        // Must not currently be in check
        if (isKingInCheck(king.getColor()))
        {
            return false;
        }

        int row  = kingSrc.getRow();
        boolean kingside = (kingDest.getCol() == 6);
        int rookCol   = kingside ? 7 : 0;
        int rookDestCol = kingside ? 5 : 3;

        Piece rookPiece = board.getPiece(new Position(row, rookCol));
        if (!(rookPiece instanceof Rook))
        {
            return false;
        }
        if (((Rook) rookPiece).hasMoved())
        {
            return false;
        }

        // Path between king and rook must be clear
        if (!isPathClear(kingSrc, new Position(row, rookCol)))
        {
            return false;
        }

        // King must not pass through or land on a square under attack
        int colStep = kingside ? 1 : -1;
        for (int c = kingSrc.getCol() + colStep; ; c += colStep) {
            // Temporarily move king to test each square
            board.movePiece(kingSrc, new Position(row, c));
            boolean inCheck = isKingInCheck(king.getColor());
            board.movePiece(new Position(row, c), kingSrc);
            if (inCheck)
            {
                return false;
            }
            if (c == kingDest.getCol())
            {
                break;
            }
        }

        // ── Execute castling ────────────
        Position rookSrc  = new Position(row, rookCol);
        Position rookDest = new Position(row, rookDestCol);

        Move move = new Move(kingSrc, kingDest, king, null);
        move.setHalfMoveClockBefore(halfMoveClock);
        move.markCastling(rookSrc, rookDest);

        board.movePiece(kingSrc, kingDest);
        board.movePiece(rookSrc, rookDest);

        king.setHasMoved(true);
        ((Rook) rookPiece).setHasMoved(true);

        enPassantTarget = null;
        halfMoveClock++;   // castling is not a pawn move or capture

        moveHistory.add(move);
        turn.switchTurn();
        return true;
    }

    // =============================
    // UNDO MOVE
    // =============================

    /**
     * Fully reverses a move, restoring all board and state fields.
     * Used internally by the check-simulation loop.
     */
    public void undoMove(Move move) {
        Position src  = move.getSource();
        Position dest = move.getDestination();
        Piece    piece = move.getPieceMoved();

        if (move.isCastling()) {
            // Move king back
            board.movePiece(dest, src);
            // Move rook back
            board.movePiece(move.getRookDest(), move.getRookSrc());
            // Restore hasMoved flags
            if (piece instanceof King) ((King) piece).setHasMoved(false);
            Piece rook = board.getPiece(move.getRookSrc());
            if (rook instanceof Rook) ((Rook) rook).setHasMoved(false);

        } else if (move.isPromotion()) {
            // Remove the promoted queen, put pawn back at src
            board.removePiece(dest);
            board.placePiece(src, piece);
            piece.setPosition(src);
            // Restore any captured piece
            if (move.getPieceCaptured() != null) {
                Piece captured = move.getPieceCaptured();
                board.placePiece(dest, captured);
                captured.setPosition(dest);
            }

        } else if (move.isEnPassant()) {
            // Move attacking pawn back
            board.movePiece(dest, src);
            // Restore the captured pawn on its original square
            Piece captured = move.getEnPassantCaptured();
            board.placePiece(move.getEnPassantSquare(), captured);
            captured.setPosition(move.getEnPassantSquare());

        } else {
            // Normal move
            board.movePiece(dest, src);
            if (move.getPieceCaptured() != null) {
                Piece captured = move.getPieceCaptured();
                board.placePiece(dest, captured);
                captured.setPosition(dest);
            }
        }

        if (piece instanceof King) {
            ((King) piece).setHasMoved(move.hadMovedBefore());
        }

        if (piece instanceof Rook) {
            ((Rook) piece).setHasMoved(move.hadMovedBefore());
        }
    }

    // =========================================================================
    // CHECK DETECTION
    // =========================================================================

    public boolean isKingInCheck(Color color) {
        Position kingPos = board.findKing(color);
        if (kingPos == null) return false;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece attacker = board.getPiece(i, j);
                if (attacker == null || attacker.getColor() == color) continue;

                if (attacker instanceof Pawn) {
                    // Pawns attack only diagonally forward
                    int rowDiff = kingPos.getRow() - attacker.getPosition().getRow();
                    int colDiff = Math.abs(kingPos.getCol() - attacker.getPosition().getCol());
                    boolean forward = (attacker.getColor() == Color.WHITE) ? rowDiff == 1 : rowDiff == -1;
                    if (forward && colDiff == 1) return true;

                } else if (attacker instanceof King) {
                    // Use only normal-move geometry for king (avoid castling branch)
                    int rd = Math.abs(kingPos.getRow() - attacker.getPosition().getRow());
                    int cd = Math.abs(kingPos.getCol() - attacker.getPosition().getCol());
                    if (rd <= 1 && cd <= 1) return true;

                } else if (attacker.isValidMove(kingPos)) {
                    if (attacker instanceof Bishop || attacker instanceof Rook || attacker instanceof Queen) {
                        if (isPathClear(attacker.getPosition(), kingPos)) return true;
                    } else {
                        return true;   // Knight
                    }
                }
            }
        }
        return false;
    }

    // =========================================================================
    // LEGAL MOVE GENERATION  O(64 * 64) = O(4096)
    // =========================================================================

    public List<Move> getLegalMoves(Color color) {
        List<Move> legal = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board.getPiece(i, j);
                if (piece == null || piece.getColor() != color) continue;

                Position src = piece.getPosition();

                // ── Castling ──────────────────────────────────────────────────
                if (piece instanceof King && !((King) piece).hasMoved()) {
                    for (int destCol : new int[]{6, 2}) {

                        Position castleDest = new Position(src.getRow(), destCol);
                        // Use a temporary copy to test — tryCastle mutates state,
                        // so we check the preconditions manually here
                        if (isCastlingLegal(src, castleDest, (King) piece)) {
                            legal.add(new Move(src, castleDest, piece, null));
                        }
                    }
                }

                // ── All other destination squares ─────────────────────────────
                for (int r = 0; r < 8; r++) {
                    for (int c = 0; c < 8; c++) {
                        Position dest = new Position(r, c);
                        Move m = tryBuildLegalMove(src, dest, piece, color);
                        if (m != null) legal.add(m);
                    }
                }
            }
        }
        return legal;
    }

    /**
     * Tries to build a legal (non-castling) move from src to dest.
     * Returns the Move if legal, null otherwise.
     * Uses simulate-and-undo to check for self-check.
     */
    private Move tryBuildLegalMove(Position src, Position dest, Piece piece, Color color) {
        // Skip castling geometry — handled separately
        if (piece instanceof King && Math.abs(dest.getCol() - src.getCol()) == 2) return null;

        if (!piece.isValidMove(dest)) return null;

        Piece destPiece = board.getPiece(dest);
        if (destPiece != null && destPiece.getColor() == color) return null;

        // ── En passant check ─────────────────────────────────────────────────
        boolean isEP = false;
        Piece   epCaptured = null;
        Position epSquare  = null;

        if (piece instanceof Pawn
                && enPassantTarget != null
                && dest.getRow() == enPassantTarget.getRow()
                && dest.getCol() == enPassantTarget.getCol()
                && Math.abs(dest.getCol() - src.getCol()) == 1)
        {
            int capturedRow = src.getRow();
            epSquare   = new Position(capturedRow, dest.getCol());
            epCaptured = board.getPiece(epSquare);
            if (epCaptured != null) isEP = true;
        }

        // ── Normal pawn rules ─────────────────────────────────────────────────
        if (piece instanceof Pawn && !isEP) {
            int crow = src.getRow(), ccol = src.getCol();
            int drow = dest.getRow(), dcol = dest.getCol();
            if (dcol == ccol) {
                if (destPiece != null) return null;
                if (Math.abs(drow - crow) == 2) {
                    int mid = (crow + drow) / 2;
                    if (board.getPiece(new Position(mid, ccol)) != null) return null;
                }
            } else {
                if (destPiece == null) return null;
            }
        }

        // ── Sliding path ──────────────────────────────────────────────────────
        if (piece instanceof Rook || piece instanceof Bishop || piece instanceof Queen) {
            if (!isPathClear(src, dest)) return null;
        }

        // ── Simulate and check for self-check ─────────────────────────────────
        Move m = new Move(src, dest, piece, destPiece);
        if (isEP) {
            board.removePiece(epSquare);
            board.movePiece(src, dest);
            m.markEnPassant(epCaptured, epSquare);
        } else {
            board.movePiece(src, dest);
        }

        boolean selfInCheck = isKingInCheck(color);
        undoMove(m);

        if (selfInCheck) return null;
        return m;
    }

    /**
     * Checks castling legality without mutating game state (used by getLegalMoves).
     */
    private boolean isCastlingLegal(Position kingSrc, Position kingDest, King king) {
        if (king.hasMoved()) return false;
        if (isKingInCheck(king.getColor())) return false;

        int row      = kingSrc.getRow();
        boolean kingside = (kingDest.getCol() == 6);
        int rookCol  = kingside ? 7 : 0;

        Piece rookPiece = board.getPiece(new Position(row, rookCol));
        if (!(rookPiece instanceof Rook)) return false;
        if (((Rook) rookPiece).hasMoved()) return false;

        if (!isPathClear(kingSrc, new Position(row, rookCol))) return false;

        int colStep = kingside ? 1 : -1;
        for (int c = kingSrc.getCol() + colStep; ; c += colStep) {
            board.movePiece(kingSrc, new Position(row, c));
            boolean inCheck = isKingInCheck(king.getColor());
            board.movePiece(new Position(row, c), kingSrc);
            if (inCheck) return false;
            if (c == kingDest.getCol()) break;
        }
        return true;
    }

    // =========================================================================
    // GAME STATUS
    // =========================================================================

    public GameStatus gameStatus() {
        // Fifty-move rule
        if (halfMoveClock >= 100)
        {
            return GameStatus.DRAW_FIFTY_MOVE;
        }

        // Insufficient material
        if (isInsufficientMaterial())
        {
            return GameStatus.DRAW_INSUFFICIENT_MATERIAL;
        }

        Color current = turn.getCurrentColor();
        boolean inCheck  = isKingInCheck(current);
        boolean hasLegal = !getLegalMoves(current).isEmpty();

        if (inCheck  && !hasLegal) return GameStatus.CHECKMATE;
        if (!inCheck && !hasLegal) return GameStatus.STALEMATE;
        if (inCheck)               return GameStatus.CHECK;
        return GameStatus.ACTIVE;
    }

    // =========================================================================
    // INSUFFICIENT MATERIAL DETECTION
    // =========================================================================

    /**
     * Returns true when neither side can possibly deliver checkmate:
     *   • King vs King
     *   • King + Bishop vs King
     *   • King + Knight vs King
     *   • King + Bishop vs King + Bishop (both bishops on same colour)
     */
    private boolean isInsufficientMaterial() {
        List<Piece> white = new ArrayList<>();
        List<Piece> black = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = board.getPiece(i, j);
                if (p == null || p instanceof King)
                {
                    continue;
                }
                if (p.getColor() == Color.WHITE)
                {
                    white.add(p);
                }
                else
                {
                    black.add(p);
                }
            }
        }

        // King vs King
        if (white.isEmpty() && black.isEmpty())
        {
            return true;
        }

        // King + minor piece vs King
        if (white.isEmpty() && black.size() == 1 && isMinorPiece(black.get(0)))
        {
            return true;
        }
        if (black.isEmpty() && white.size() == 1 && isMinorPiece(white.get(0)))
        {
            return true;
        }

        // King + Bishop vs King + Bishop (same coloured squares)
        if (white.size() == 1 && black.size() == 1
                && white.get(0) instanceof Bishop
                && black.get(0) instanceof Bishop)
        {
            Position wp = white.get(0).getPosition();
            Position bp = black.get(0).getPosition();

            boolean whiteBishopOnLight = (wp.getRow() + wp.getCol()) % 2 == 0;
            boolean blackBishopOnLight = (bp.getRow() + bp.getCol()) % 2 == 0;
            if (whiteBishopOnLight == blackBishopOnLight)
            {
                return true;
            }
        }

        return false;
    }

    private boolean isMinorPiece(Piece p) {
        //Camel or horse
        return p instanceof Bishop || p instanceof Knight;
    }

    // =========================================================================
    // PATH CLEAR HELPER
    // =========================================================================

    public boolean isPathClear(Position src, Position dest) {
        int crow = src.getRow(), ccol = src.getCol();
        int drow = dest.getRow(), dcol = dest.getCol();

        while (crow != drow || ccol != dcol) {

            crow += Integer.compare(drow, crow);
            ccol += Integer.compare(dcol, ccol);
            if (crow == drow && ccol == dcol) break;
            if (board.getPiece(new Position(crow, ccol)) != null) return false;
        }
        return true;
    }

    //Simulation Move
    public void simulationMove(Move move)
    {
        Position src=move.getSource();
        Position dest=move.getDestination();

        Piece piece=move.getPieceMoved();

        // Normal capture
        if(move.getPieceCaptured()!=null)
        {
            board.removePiece( dest );
        }

        // En passant
        if(move.isEnPassant())
        {
            board.removePiece( move.getEnPassantSquare() );
        }

        // Move piece
        board.movePiece( src,dest );

        // Castling
        if (move.isCastling()) {
            board.movePiece(
                    move.getRookSrc(),
                    move.getRookDest()
            );
        }

        // Promotion
        if (move.isPromotion()) {
            board.removePiece(dest);

            board.placePiece(
                    dest,
                    move.getPromotedTo()
            );
        }
    }

    public void undoSimulation(Move move)
    {
        Position src=move.getSource();
        Position dest=move.getDestination();

        //undo promotion
        if(move.isPromotion())
        {
            board.removePiece( dest );

            board.placePiece(
                    src,
                    move.getPieceMoved()
            );

        }
        else
        {
            // Move piece back
            board.movePiece( dest,src );
        }

        // Restore normal captured piece
        if(move.getPieceCaptured()!=null)
        {
            board.placePiece(
                    dest,
                    move.getPieceCaptured()
            );
        }
        // Restore en passant captured pawn
        if(move.isEnPassant())
        {
            board.placePiece(
                    move.getEnPassantSquare(),
                    move.getEnPassantCaptured()
            );
        }

        //undo castling
        if(move.isCastling())
        {
            board.movePiece(
                    move.getRookDest(),
                    move.getRookSrc()
            );
        }

        // Restore King/Rook moved state
        Piece piece=move.getPieceMoved();

        if(piece instanceof King)
        {
            ((King)piece).setHasMoved(
                    move.hadMovedBefore()
            );
        }

        if(piece instanceof Rook)
        {
            ((Rook)piece).setHasMoved(
                    move.hadMovedBefore()
            );
        }
    }






}
