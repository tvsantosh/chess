package game;

import pieces.Color;


public class Turn {

    private Color currentColor;


    public Turn()
    {
        currentColor=Color.WHITE;
    }


    public Color getCurrentColor() {
        return currentColor;
    }

    public void switchTurn()
    {
        if(currentColor==Color.WHITE)
        {
            currentColor=Color.BLACK;
        }
        else
        {
            currentColor=Color.WHITE;
        }
    }
}
