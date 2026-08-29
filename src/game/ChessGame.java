package game;

import board.Board;
import board.Move;
import board.Position;
import pieces.*;
import player.Player;

import java.util.ArrayList;
import java.util.List;


public class ChessGame {

    private Board board;

    private Turn turn;

    private Player whitePlayer;

    private Player blackPlayer;

    private List<Move> movehistory;

    public Board getBoard() {
        return board;
    }

    public Turn getTurn() {
        return turn;
    }


    public ChessGame()
    {
        board=new Board();
        turn =new Turn();

        whitePlayer = new Player( Color.WHITE );
        blackPlayer = new Player( Color.BLACK );

        movehistory=new ArrayList<>(  );

        setupBoard();
    }

    //Initialize the board
    public void setupBoard() {

        // =========================
        // White Pieces----TOP
        // =========================

        //ROOK(ELEPHANT-LEFTSIDE)
        Position pos = new Position(0, 0);
        Rook whiteRook1 = new Rook(pos, Color.WHITE);
        board.placePiece(pos, whiteRook1);

        //KNIGHT(HORSE-LEFTSIDE)
        pos = new Position(0, 1);
        Knight whiteKnight1 = new Knight(pos, Color.WHITE);
        board.placePiece(pos, whiteKnight1);

        //BISHOP(CAMEL-LEFTSIDE)
        pos = new Position(0, 2);
        Bishop whiteBishop1 = new Bishop(pos, Color.WHITE);
        board.placePiece(pos, whiteBishop1);

        //QUEEN********************************************
        pos = new Position(0, 3);
        Queen whiteQueen = new Queen(pos, Color.WHITE);
        board.placePiece(pos, whiteQueen);

        //KING*********************************************
        pos = new Position(0, 4);
        King whiteKing = new King(pos, Color.WHITE);
        board.placePiece(pos, whiteKing);

        //BISHOP(CAMEL-RIGHTSIDE)
        pos = new Position(0, 5);
        Bishop whiteBishop2 = new Bishop(pos, Color.WHITE);
        board.placePiece(pos, whiteBishop2);

        //KNIGHT(HORSE-RIGHTSIDE)
        pos = new Position(0, 6);
        Knight whiteKnight2 = new Knight(pos, Color.WHITE);
        board.placePiece(pos, whiteKnight2);

        //ROOK(ELEPHANT-RIGHTSIDE)
        pos = new Position(0, 7);
        Rook whiteRook2 = new Rook(pos, Color.WHITE);
        board.placePiece(pos, whiteRook2);


        //PAWN(SOLDIER -WHITE)
        for(int i=0;i<8;i++)
        {
            pos=new Position( 1,i );
            Pawn pawn=new Pawn( pos,Color.WHITE );
            board.placePiece( pos,pawn );
        }


        // =========================
        // Black Pieces---DOWN
        // =========================

        //ROOK(ELEPHANT-LEFTSIDE)
        pos = new Position(7, 0);
        Rook blackRook1 = new Rook(pos, Color.BLACK);
        board.placePiece(pos, blackRook1);

        //KNIGHT(HORSE-LEFTSIDE)
        pos = new Position(7, 1);
        Knight blackKnight1 = new Knight(pos, Color.BLACK);
        board.placePiece(pos, blackKnight1);

        //BISHOP(CAMEL-LEFTSIDE)
        pos = new Position(7, 2);
        Bishop blackBishop1 = new Bishop(pos, Color.BLACK);
        board.placePiece(pos, blackBishop1);

        //QUEEN***************************************************
        pos = new Position(7, 3);
        Queen blackQueen = new Queen(pos, Color.BLACK);
        board.placePiece(pos, blackQueen);

        //KING****************************************************
        pos = new Position(7, 4);
        King blackKing = new King(pos, Color.BLACK);
        board.placePiece(pos, blackKing);

        //BISHOP(CAMEL-RIGHTSIDE)
        pos = new Position(7, 5);
        Bishop blackBishop2 = new Bishop(pos, Color.BLACK);
        board.placePiece(pos, blackBishop2);

        //KNIGHT(HORSE-RIGHTSIDE)
        pos = new Position(7, 6);
        Knight blackKnight2 = new Knight(pos, Color.BLACK);
        board.placePiece(pos, blackKnight2);

        //ROOK(ELEPHANT-RIGHTSIDE)
        pos = new Position(7, 7);
        Rook blackRook2 = new Rook(pos, Color.BLACK);
        board.placePiece(pos, blackRook2);

        //PAWN(SOLDIER -BLACK)
        for(int i=0;i<8;i++)
        {
            pos=new Position( 6,i );
            Pawn pan=new Pawn( pos,Color.BLACK );
            board.placePiece( pos,pan );

        }
    }


    public boolean move(Position src, Position dest)
    {
        if (!src.isValid() || !dest.isValid())
        {
            return false;
        }

        Piece piece = board.getPiece(src);
        Piece destpiece = board.getPiece(dest);


        if (piece == null)
        {
            return false;
        }

        if (piece.getColor() != turn.getCurrentColor())
        {
            return false;
        }

        if(!piece.isValidMove( dest ))
        {
            return false;

        }

        if ( destpiece!=null && destpiece.getColor()==turn.getCurrentColor() )
        {
            return false;
        }

        if(piece instanceof Pawn)
        {
            int crow = src.getRow();
            int ccol = src.getCol();

            int drow = dest.getRow();
            int dcol = dest.getCol();

            // Pawn moving straight forward
            if(dcol == ccol)
            {
                // Destination must be empty
                if(destpiece != null)
                {
                    return false;
                }

                // Two-square move
                if(Math.abs(drow - crow) == 2)
                {
                    int middleRow = (crow + drow) / 2;

                    // Middle square must be empty
                    if(board.getPiece(new Position(middleRow, ccol)) != null)
                    {
                        return false;
                    }
                }


            }

            // Pawn moving diagonally
            else if(Math.abs(dcol - ccol) == 1)
            {
                // Diagonal move must capture an enemy piece
                if(destpiece == null)
                {
                    return false;
                }
            }
        }



        if(piece instanceof  Rook ||
                piece instanceof Bishop ||
                piece instanceof Queen)
        {
            if( !isPathClear( src,dest ))
            {
                return false;
            }
        }


        Move move = new Move(src, dest, piece, destpiece);
        board.movePiece( src, dest );
        if(isKingInCheck( piece.getColor() ))
        {
            undoMove( move );
            return false;
        }

        turn.switchTurn();
        return true;

    }

    public boolean isPathClear(Position src, Position dest)
    {
        int crow = src.getRow();
        int ccol = src.getCol();

        int drow = dest.getRow();
        int dcol = dest.getCol();

        while (crow != drow || ccol != dcol)
        {
            int rowStep = Integer.compare(drow, crow);
            int colStep = Integer.compare(dcol, ccol);

            crow += rowStep;
            ccol += colStep;

            // Don't check the destination here.
            if (crow == drow && ccol == dcol)
            {
                break;
            }

            // Check the current intermediate square.
            if (board.getPiece(new Position(crow, ccol)) != null)
            {
                return false;
            }
        }

        return true;
    }
    private boolean isKingInCheck(Color color)
    {
        Position kingpos = board.findKing(color);

        if (kingpos == null)
        {
            return false;
        }

        for (int i = 0; i < 8; i++)
        {
            for (int j = 0; j < 8; j++)
            {
                Piece piece = board.getPiece(i, j);

                if (piece == null)
                {
                    continue;
                }

                if (piece.getColor() != color)
                {
                    if (piece instanceof Pawn)
                    {
                        // Pawns only attack diagonally — never straight ahead
                        int rowDiff = kingpos.getRow() - piece.getPosition().getRow();
                        int colDiff = Math.abs(kingpos.getCol() - piece.getPosition().getCol());
                        boolean correctDirection = (piece.getColor() == Color.WHITE) ? rowDiff == 1 : rowDiff == -1;
                        if (correctDirection && colDiff == 1)
                        {
                            return true;
                        }
                    }
                    else if (piece.isValidMove(kingpos))
                    {
                        if (piece instanceof Bishop ||
                                piece instanceof Rook ||
                                piece instanceof Queen)
                        {
                            if (isPathClear(piece.getPosition(), kingpos))
                            {
                                return true;
                            }
                        }
                        else
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public void undoMove(Move move)
    {
        // your code here

        board.movePiece(move.getDestination(), move.getSource());

        if(move.getPieceCaptured()!=null)
        {
            board.placePiece( move.getDestination(),move.getPieceCaptured() );
        }

    }


    //O(4096)
    public List<Move> getLegalMoves(Color color)
    {
        List<Move> legalMoves = new ArrayList<>();

        for (int i = 0; i < 8; i++)
        {
            for (int j = 0; j < 8; j++)
            {
                Piece piece = board.getPiece(new Position(i, j));

                if (piece == null)
                {
                    continue;
                }

                if (piece.getColor() != color)
                {
                    continue;
                }

                Position src = new Position(i, j);

                // Try every destination
                for (int r = 0; r < 8; r++)
                {
                    for (int c = 0; c < 8; c++)
                    {
                        Position dest = new Position(r, c);

                        // Check piece movement
                        if (!piece.isValidMove(dest))
                        {
                            continue;
                        }

                        // Check destination
                        Piece destPiece = board.getPiece(dest);

                        // Cannot capture own piece
                        if (destPiece != null &&
                                destPiece.getColor() == color)
                        {
                            continue;
                        }

                        // Pawn-specific board rules
                        if (piece instanceof Pawn)
                        {
                            int crow = src.getRow();
                            int ccol = src.getCol();
                            int drow = dest.getRow();
                            int dcol = dest.getCol();

                            if (dcol == ccol)
                            {
                                // Straight push — destination must be empty
                                if (destPiece != null)
                                {
                                    continue;
                                }
                                // Double push — middle square must also be empty
                                if (Math.abs(drow - crow) == 2)
                                {
                                    int middleRow = (crow + drow) / 2;
                                    if (board.getPiece(new Position(middleRow, ccol)) != null)
                                    {
                                        continue;
                                    }
                                }
                            }
                            else
                            {
                                // Diagonal move — must capture an enemy piece
                                if (destPiece == null)
                                {
                                    continue;
                                }
                            }
                        }

                        // Sliding pieces need clear path
                        if (piece instanceof Rook ||
                                piece instanceof Bishop ||
                                piece instanceof Queen)
                        {
                            if (!isPathClear(src, dest))
                            {
                                continue;
                            }
                        }

                        // Simulate the move and reject if it leaves own king in check
                        Move move = new Move(src, dest, piece, destPiece);
                        board.movePiece(src, dest);
                        boolean inCheck = isKingInCheck(color);
                        undoMove(move);
                        if (inCheck)
                        {
                            continue;
                        }

                        legalMoves.add(move);
                    }
                }
            }
        }

        return legalMoves;
    }


}
