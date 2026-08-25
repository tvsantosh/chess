package pieces;

import board.Position;


public abstract class Piece {

    private Position position;
    private Color color;

    public abstract boolean isValidMove(Position destination);

    public Piece( Position position , Color color ) {
        this.position = position;
        this.color = color;
    }

    public Position getPosition() {
        return position;
    }

    public Color getColor() {
        return color;
    }

    public void setPosition( Position position ) {
        this.position = position;
    }


}
