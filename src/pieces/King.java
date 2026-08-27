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

        int rowDiff = Math.abs( crow - drow );
        int colDiff = Math.abs( ccol - dcol );

        // King moves exactly one square in any direction (but not staying in place)
        return (rowDiff <= 1 && colDiff <= 1) && !(crow == drow && ccol == dcol);
    }


}
