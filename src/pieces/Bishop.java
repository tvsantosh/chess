package pieces;

import board.Position;

public class Bishop extends Piece {

    public Bishop( Position position,Color color )
    {
        super(position,color);
    }

    public boolean isValidMove(Position destination) {

        if(!destination.isValid())
        {
            return false;
        }


        int crow = getPosition( ).getRow( );
        int ccol = getPosition( ).getCol( );

        int drow = destination.getRow( );
        int dcol = destination.getCol( );



       return Math.abs( drow - crow ) == Math.abs( dcol - ccol ) && !(crow==drow && ccol==dcol);
    }

}

