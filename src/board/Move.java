package board;

import pieces.Piece;

public class Move {
    private Position source;
    private Position destination;
    private Piece pieceMoved;
    private Piece pieceCaptured;



    public Move( Position source , Position destination , Piece pieceMoved , Piece pieceCaptured )
    {
        this.source = source;
        this.destination = destination;
        this.pieceMoved = pieceMoved;
        this.pieceCaptured = pieceCaptured;
    }

    public Position getSource() {
        return source;
    }

    public Position getDestination() {
        return destination;
    }

    public Piece getPieceMoved() {
        return pieceMoved;
    }

    public Piece getPieceCaptured() {
        return pieceCaptured;
    }

}
