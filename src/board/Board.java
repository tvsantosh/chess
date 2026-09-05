package board;

import pieces.Color;
import pieces.King;
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
        if (src == null || dest == null ||
                !src.isValid() || !dest.isValid())
        {
            return;
        }

        Piece piece = board[src.getRow()][src.getCol()];

        if (piece == null)
        {
            return;
        }

        if(board[dest.getRow()][dest.getCol()]!=null)
        {
            removePiece( dest );
        }

        board[dest.getRow()][dest.getCol()] = piece;

        piece.setPosition(dest);

        board[src.getRow()][src.getCol()] = null;
    }


    public Piece getPiece(int row,int col)
    {
        return board[row][col];
    }

    public Position findKing( Color color )
    {
        for(int i=0;i<8;i++)
        {
            for(int j=0;j<8;j++)
            {
                Piece piece=getPiece( i,j );

                if(piece instanceof King && piece.getColor()==color)
                {
                    return piece.getPosition();
                }
            }
        }
        return null;
    }

    public void removePiece(Position position)
    {
        if(position!=null && position.isValid( ))

        board[position.getRow()][position.getCol()]=null;
    }



}
