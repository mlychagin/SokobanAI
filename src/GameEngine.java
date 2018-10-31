import java.util.ArrayList;

public class GameEngine{
    BoardState root;
    int height = 0;
    int width = 0;

    public GameEngine(){
    }

    public ArrayList<Byte> possibleMoves(){
        return null;
    }

    public void initBoard(int height, int width, int[] walls, int[] boxes, int[] goals, int row, int column){
        root = new BoardState(height, width);
        root.setWalls(walls);
        root.setBoxes(boxes);
        root.setGoals(goals);
        root.setPlayerCoordinates(row-1, column-1);
        this.height = height;
        this.width = width;
    }

    public boolean isWinnable(int move){
        return false;
    }
}
