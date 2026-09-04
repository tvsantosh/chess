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

    // =========================
    // PIECE VALUES
    // =========================

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

    // =========================
    // BEST CAPTURE
    // =========================
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
    // =========================
    // BOARD EVALUATION
    // =========================

    private int evaluateBoard(ChessGame game) {

        int score = 0;

        for (int row = 0; row < 8; row++) {

            for (int col = 0; col < 8; col++) {

                Piece piece = game.getBoard()
                        .getPiece(new board.Position(row, col));

                if (piece == null) {
                    continue;
                }

                int value = getPieceValue(piece);

                if (piece.getColor() == Color.WHITE) {
                    score += value;
                } else {
                    score -= value;
                }
            }
        }

        return score;
    }



    // =========================
    // MINMAX ALGORITHM
    // =========================
    private int minimax(
            ChessGame game,
            int depth,
            boolean maximizingPlayer,
            Color aiColor) {

        // Stop searching when depth reaches 0
        if (depth == 0) {
            int evaluation = evaluateBoard(game);

            // AI is Black, so reverse the score
            if (aiColor == Color.BLACK) {
                return -evaluation;
            }

            return evaluation;
        }

        Color currentColor;

        if (maximizingPlayer) {
            currentColor = aiColor;
        } else {
            currentColor = getOpponentColor(aiColor);
        }

        List<Move> legalMoves =
                game.getLegalMoves(currentColor);

        // No legal moves
        if (legalMoves.isEmpty()) {

            if (game.isKingInCheck(currentColor)) {

                // Checkmate
                if (currentColor == aiColor) {
                    return -1000000;
                } else {
                    return 1000000;
                }
            }

            // Stalemate
            return 0;
        }

        if (maximizingPlayer) {

            int bestScore = Integer.MIN_VALUE;

            for (Move move : legalMoves) {

                // We will add safe simulation here
                // in the next step.

            }

            return bestScore;

        } else {

            int bestScore = Integer.MAX_VALUE;

            for (Move move : legalMoves) {

                // We will add safe simulation here
                // in the next step.

            }

            return bestScore;
        }
    }

    private Color getOpponentColor(Color color) {

        if (color == Color.WHITE) {
            return Color.BLACK;
        }

        return Color.WHITE;
    }

}
