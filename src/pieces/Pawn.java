package pieces;

import board.Position;

public class Pawn extends Piece {

    public Pawn(Position position, Color color) {
        super(position, color);
    }

    public boolean isValidMove(Position destination) {

        if (!destination.isValid()) {
            return false;
        }

        int drow = destination.getRow();
        int dcol = destination.getCol();

        int crow = getPosition().getRow();
        int ccol = getPosition().getCol();

        if (crow == drow && ccol == dcol) {
            return false;
        }

        if (getColor() == Color.WHITE) {

            // One step forward
            if (drow == crow + 1 && dcol == ccol) {
                return true;
            }

            // Two steps forward from starting position
            if (crow == 1 && drow == crow + 2 && dcol == ccol) {
                return true;
            }

            //Diagonal moment
            if((drow==crow+1 && Math.abs( dcol-ccol )==1) )
            {
                return true;

            }

        } else {

            // One step forward
            if (drow == crow - 1 && dcol == ccol) {
                return true;
            }

            // Two steps forward from starting position
            if (crow == 6 && drow == crow - 2 && dcol == ccol) {
                return true;
            }

            //Diagonal moment
            if(drow==crow-1 && Math.abs( dcol-ccol )==1)
            {
                return true;
            }
        }

        return false;
    }
}