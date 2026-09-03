package ai;

import board.Move;
import game.ChessGame;
import pieces.*;

import java.util.List;
import java.util.Random;

public class ChessAi {

    private final Random random=new Random(  );

    public Move findMove( ChessGame game, Color aiColor )
    {
        List<Move> leagelMoves=
                game.getLegalMoves( aiColor );

        if(leagelMoves.isEmpty())
        {
            return null;
        }
        //getMove
        Move capture=findBestCapture( game,leagelMoves,aiColor );

        if(capture!=null)
        {
            return capture;
        }


        return leagelMoves.get( random.nextInt(leagelMoves.size()) );
    }

    private int getPieceValue(Piece piece)
    {
        if(piece instanceof Pawn )
        {
            return 100;
        }
        if(piece instanceof Knight )
        {
            return 320;
        }
        if ( piece instanceof Bishop )
        {
            return 330;
        }
        if(piece instanceof Rook)
        {
            return 500;
        }
        if(piece instanceof Queen)
        {
            return 900;
        }
        if(piece instanceof King)
        {
            return 2000;
        }
        return 0;
    }

    private Move findBestCapture(ChessGame game,List<Move> legaleMoves,Color aiColor)
    {
        Move bestMove=null;

        int bestValue=-1;

        for ( Move move:legaleMoves )
        {
            Piece target=game.getBoard().getPiece( move.getDestination() );

            if(target==null)
            {
                continue;
            }
            if ( target.getColor()==aiColor )
            {
                continue;
            }
            int value=getPieceValue( target );
            if(value>bestValue)
            {
                bestValue=value;
                bestMove=move;
            }
        }
        return bestMove;
    }



}
