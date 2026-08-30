package board;

import pieces.Piece;

/**
 * Immutable-ish record of one half-move (ply).
 *
 * Special move flags:
 *   isEnPassant   – pawn captured an adjacent pawn via en-passant rule
 *   isCastling    – king moved two squares left or right
 *   isPromotion   – pawn reached the back rank (promotedTo is non-null)
 *
 * Snapshot fields used for perfect undo:
 *   enPassantCaptured  – the pawn removed from the board during en-passant
 *   halfMoveClockBefore – value of the half-move clock BEFORE this move,
 *                         so it can be restored on undo
 */
public class Move {

    // ── Core fields ───────────────────────────────────────────────────────────
    private final Position source;
    private final Position destination;
    private final Piece    pieceMoved;
    private final Piece    pieceCaptured;       // null if no normal capture

    // ── Special-move flags (set after construction) ───────────────────────────
    private boolean isEnPassant  = false;
    private boolean isCastling   = false;
    private Piece   promotedTo   = null;        // non-null → promotion

    // ── En-passant specific ───────────────────────────────────────────────────
    private Piece    enPassantCaptured = null;  // the pawn removed off-square
    private Position enPassantSquare   = null;  // square the captured pawn stood on

    // ── Castling specific ─────────────────────────────────────────────────────
    private Position rookSrc  = null;           // rook's original square
    private Position rookDest = null;           // rook's destination square

    // ── Fifty-move-rule snapshot ──────────────────────────────────────────────
    private int halfMoveClockBefore = 0;

    // ─────────────────────────────────────────────────────────────────────────
    public Move(Position source,
                Position destination,
                Piece   pieceMoved,
                Piece   pieceCaptured)
    {
        this.source        = source;
        this.destination   = destination;
        this.pieceMoved    = pieceMoved;
        this.pieceCaptured = pieceCaptured;
    }

    public Position getSource()
    {
        return source;
    }
    public Position getDestination()
    {
        return destination;
    }
    public Piece getPieceMoved()
    {
        return pieceMoved;
    }
    public Piece getPieceCaptured()
    {
        return pieceCaptured;
    }

    public boolean  isEnPassant()
    {
        return isEnPassant;
    }
    public boolean  isCastling()
    {
        return isCastling;
    }
    public boolean  isPromotion()
    {
        return promotedTo != null;
    }
    public Piece    getPromotedTo()
    {
        return promotedTo;
    }
    public Piece    getEnPassantCaptured()
    {
        return enPassantCaptured;
    }
    public Position getEnPassantSquare()
    {
        return enPassantSquare;
    }
    public Position getRookSrc()
    {
        return rookSrc;
    }
    public Position getRookDest()
    {
        return rookDest;
    }

    public int getHalfMoveClockBefore()
    {
        return halfMoveClockBefore;
    }

    // ── Setters (called by ChessGame after construction) ──────────────────────
    public void setPromotedTo(Piece p)           { this.promotedTo = p; }

    public void markEnPassant(Piece captured, Position capturedSquare) {
        this.isEnPassant       = true;
        this.enPassantCaptured = captured;
        this.enPassantSquare   = capturedSquare;
    }

    public void markCastling(Position rookSrc, Position rookDest) {
        this.isCastling = true;
        this.rookSrc    = rookSrc;
        this.rookDest   = rookDest;
    }

    public void setHalfMoveClockBefore(int v) { this.halfMoveClockBefore = v; }

    // ── Human-readable label ─────────────────────────────────────────────────
    @Override
    public String toString() {

        if (isCastling) {
            // King moves right = kingside (O-O), left = queenside (O-O-O)
            return (destination.getCol() == 6) ? "O-O" : "O-O-O";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(pieceLabel(pieceMoved));
        sb.append(squareLabel(source));
        sb.append(pieceCaptured != null || isEnPassant ? "x" : "-");
        sb.append(squareLabel(destination));
        if (isPromotion()) sb.append("=Q");
        return sb.toString();
    }

    private static String pieceLabel(Piece p) {
        if (p instanceof pieces.King)
        {
            return "K";
        }

        if (p instanceof pieces.Queen)
        {
            return "Q";
        }
        if (p instanceof pieces.Rook)
        {
            return "R";
        }

        if (p instanceof pieces.Bishop)
        {
            return "B";
        }

        if (p instanceof pieces.Knight)
        {
            return "N";
        }

        return "";
    }

    private static String squareLabel(Position p) {
        return "" + (char)('a' + p.getCol()) + (p.getRow() + 1);
    }
}
