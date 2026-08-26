package board;

import pieces.Piece;

public class Board {


    private Piece[][] board=new Piece[8][8];


    public Piece getPiece(Position position)
    {
        if(position.isValid())
        {
            return board[position.getRow()][position.getCol()];
        }
        return null;

    }

    public void placePiece(Position position,Piece piece)
    {
        if(position.isValid())
        {
            board[ position.getRow( )][ position.getCol( )]=piece;

        }

    }

    public void movePiece(Position src, Position dest)
    {
        if(src.isValid() && dest.isValid() &&
                board[src.getRow()][src.getCol()] != null )
        {
            Piece piece = board[src.getRow()][src.getCol()];

            if (!piece.isValidMove(dest)) {
                return;
            }

            board[dest.getRow()][dest.getCol()] = piece;

            piece.setPosition(dest);

            board[src.getRow()][src.getCol()] = null;
        }
    }



}
