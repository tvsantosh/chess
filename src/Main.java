import board.Board;
import board.Position;
import game.ChessGame;
import game.Turn;
import pieces.*;

public class Main {
    public static void main(String[] args)
    {
        ChessGame game=new ChessGame();

       System.out.println(game.move(new Position(0,0), new Position(0,5)));
    }
}
