import board.Board;
import board.Position;
import game.ChessGame;
import game.Turn;
import pieces.*;

public class Main {
    public static void main(String[] args)
    {
        ChessGame game=new ChessGame();

        for(int i=0;i<8;i++)
        {
            for ( int j=0;j<8;j++)
            {

                Piece piece = game.getBoard().getPiece(new Position(i, j));

                System.out.print(piece != null ? piece : "."+" ");

            }
            System.out.println(  );
        }
        game.move( new Position( 1,0 ),new Position( 2,0 ) );





        for(int i=0;i<8;i++)
        {
            for ( int j=0;j<8;j++)
            {

                Piece piece = game.getBoard().getPiece(new Position(i, j));

                System.out.print(piece != null ? piece : "."+" ");

            }
            System.out.println(  );
        }


    }
}
