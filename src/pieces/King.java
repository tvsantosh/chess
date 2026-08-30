package pieces;

import board.Position;

public class King extends Piece {

    /**
     * Set to true the first time this King actually moves.
     * Used by castling logic — castling is only legal if the king has never moved.
     */
    private boolean hasMoved = false;

    public King(Position position, Color color) {
        super(position, color);
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    @Override
    public boolean isValidMove(Position destination) {
        if (!destination.isValid()) return false;

        int crow = getPosition().getRow();
        int ccol = getPosition().getCol();
        int drow = destination.getRow();
        int dcol = destination.getCol();

        int rowDiff = Math.abs(crow - drow);
        int colDiff = Math.abs(ccol - dcol);

        // Normal king move: one square in any direction
        if (rowDiff <= 1 && colDiff <= 1 && !(crow == drow && ccol == dcol)) {
            return true;
        }

        // Castling geometry: king slides exactly two squares horizontally
        // (board-level legality — path clear, not in check — is enforced in ChessGame)
        if (rowDiff == 0 && colDiff == 2) {
            return true;
        }

        return false;
    }
}
