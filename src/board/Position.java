package board;

public class Position {

    private int row;
    private int col;


    public Position( int row , int col ) {
        this.row = row;
        this.col = col;
    }

    public boolean isValid()
    {

     if(row>=0 && row<8 && col>=0 && col<8)
     {
         return true;
     }

     return false;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
