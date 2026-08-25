package pieces;

import board.Position;

public class Knight extends Piece {

    public Knight( Position position,Color color )
    {
        super(position,color);
    }

    public boolean isValidMove(Position destination)
    {
        if(!destination.isValid())
        {
            return false;
        }

        int crow=getPosition().getRow();
        int ccol=getPosition().getCol();

        int drow=destination.getRow();
        int dcol=destination.getCol();

        return  (Math.abs( drow-crow )==2 && Math.abs( dcol-ccol )==1) ||
                (Math.abs( drow-crow )==1 && Math.abs( dcol-ccol )==2) && !(crow==drow && ccol==dcol);

    }
}
