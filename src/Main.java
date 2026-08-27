import board.Board;
import board.Position;
import game.ChessGame;
import game.Turn;
import pieces.*;

public class Main {
    public static void main(String[] args)
    {
        ChessGame game=new ChessGame();

       System.out.println(game.move(new Position(1,1), new Position(3,1)));
        System.out.println(game.move(new Position(6,2), new Position(4,2)));
        System.out.println(game.move(new Position(1,2), new Position(2,2)));
        System.out.println(game.move(new Position(4,2), new Position(3,1)));
        System.out.println(game.move(new Position(3,1), new Position(4,1)));

    }
}
