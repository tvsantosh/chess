import board.Move;
import board.Position;
import game.ChessGame;
import pieces.Color;

import java.util.List;

public class Main {
    public static void main(String[] args)
    {
        ChessGame game = new ChessGame();

        List<Move> moves = game.getLegalMoves(Color.WHITE);

        System.out.println("White legal moves: " + moves.size());
    }
}