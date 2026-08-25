package pieces;

import board.Position;

public class King extends Piece{

    public King( Position position,Color color )
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

        return (Math.abs( crow - drow )==0 && Math.abs( ccol - dcol )==1) ||
                (Math.abs( crow-drow )==1 && Math.abs( ccol - dcol )==0) ||
                (Math.abs( crow - drow )==1 && Math.abs( ccol - dcol )==1) &&
                        !(crow==drow && ccol==dcol);
    }


}
