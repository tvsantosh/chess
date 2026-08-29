import board.Move;
import board.Position;
import game.ChessGame;
import pieces.Color;

public class Main {
    public static void main(String[] args)
    {
        ChessGame game = new ChessGame();

        for (Move move : game.getLegalMoves(Color.WHITE))
        {
            System.out.println(
                    "(" +
                            move.getSource().getRow() +
                            "," +
                            move.getSource().getCol() +
                            ")  ->  (" +
                            move.getDestination().getRow() +
                            "," +
                            move.getDestination().getCol() +
                            ")"
            );
        }
        System.out.println( game.getLegalMoves(Color.WHITE).size() );
    }
}