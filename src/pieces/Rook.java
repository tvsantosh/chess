package pieces;

import board.Position;

public class Rook extends Piece {

    public Rook(Position position,Color color)
    {
        super(position,color);
    }

    public boolean isValidMove(Position destination) {


        if ( !destination.isValid() )
        {
            return false;
        }

        int drow = destination.getRow();
        int dcol = destination.getCol();

        int crow = getPosition().getRow();
        int ccol = getPosition().getCol();


        return ( crow==drow || ccol==dcol ) && !(crow==drow && ccol==dcol);
    }




}
