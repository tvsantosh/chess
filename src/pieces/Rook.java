package pieces;

import board.Position;

public class Rook extends Piece {

    /**
     * Set to true the first time this Rook actually moves (or is captured).
     * Used by castling logic — castling is only legal if the rook has never moved.
     */
    private boolean hasMoved = false;

    public Rook(Position position, Color color) {
        super(position, color);
    }

    public boolean hasMoved() { return hasMoved; }

    public void setHasMoved(boolean hasMoved) { this.hasMoved = hasMoved; }

    @Override
    public boolean isValidMove(Position destination) {
        if (!destination.isValid()) return false;

        int drow = destination.getRow();
        int dcol = destination.getCol();
        int crow = getPosition().getRow();
        int ccol = getPosition().getCol();

        return (crow == drow || ccol == dcol) && !(crow == drow && ccol == dcol);
    }
}
