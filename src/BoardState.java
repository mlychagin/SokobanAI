import java.util.ArrayList;
import java.util.Comparator;

public class BoardState{
    ArrayList<ArrayList<Byte>> board;
    private int rowPos;
    private int columnPos;

    BoardState parent = null;
    int totalMoves = 0;
    ArrayList<Byte> movesFromParent;

    public BoardState(){
        board = new ArrayList<>();
        movesFromParent = new ArrayList<>();
    }

    public void updatePlayerLocation(){
        setCoordinate(rowPos, columnPos, getCoordinate(rowPos, columnPos) == Util.goal ? Util.playerOnGoal : Util.player);
    }

    public void setPlayerCoordinates(int x, int y){
        rowPos = x;
        columnPos = y;
    }

    void setCoordinate(int x, int y, byte value){
        board.get(x).set(y, value);
    }

    byte getCoordinate(int x, int y){
        return board.get(x).get(y);
    }

    private boolean moveBox(int rowStart, int columnStart, int rowEnd, int columnEnd, byte direction){
        //TODO detect if moving box results in unwinnable state
        byte endSlot = getCoordinate(rowEnd, columnEnd);
        boolean isStartBox = getCoordinate(rowStart, columnStart) == Util.box;
        boolean isEndEmpty = getCoordinate(rowEnd, columnEnd) == Util.empty;
        switch(endSlot){
            case Util.empty:
                setCoordinate(rowStart, columnStart, isStartBox ? Util.empty : Util.goal);
                setCoordinate(rowEnd, columnEnd, Util.box);
            case Util.goal:
                break;
            default:
                return false;
        }
        setCoordinate(rowEnd, columnEnd, isEndEmpty ? Util.box : Util.boxOnGoal);
        updatePlayerPositionAfterMoving(direction);
        return true;
    }

    private void updatePlayerPositionAfterMoving(byte direction){
        setCoordinate(rowPos, columnPos, getCoordinate(rowPos, columnPos) == Util.playerOnGoal ? Util.goal : Util.empty);
        switch (direction){
            case Util.up:
                rowPos--;
                break;
            case Util.down:
                rowPos++;
                break;
            case Util.right:
                columnPos++;
                break;
            case Util.left:
                columnPos--;
                break;
        }
        updatePlayerLocation();
    }

    public byte move(byte direction){
        byte offsetRow = 0;
        byte offsetColumn = 0;
        switch (direction){
            case Util.up:
                offsetRow = -1;
                break;
            case Util.down:
                offsetRow = 1;
                break;
            case Util.right:
                offsetColumn = 1;
                break;
            case Util.left:
                offsetColumn = -1;
                break;
        }
        switch (getCoordinate(rowPos + offsetRow, columnPos + offsetColumn)){
            case Util.box:
            case Util.boxOnGoal:
                return moveBox(rowPos +offsetRow, columnPos +offsetColumn, rowPos +offsetRow*2, columnPos +offsetColumn*2, direction) ? Util.boxMove : Util.invalidMove;
            case Util.empty:
            case Util.goal:
                break;
            default:
                return Util.invalidMove;
        }
        updatePlayerPositionAfterMoving(direction);
        return Util.playerMove;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (ArrayList<Byte> row : board) {
            for (byte column : row) {
                builder.append((char)column).append(" ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    public static class BoardStateCompare implements Comparator<BoardState> {
        @Override
        public int compare(BoardState o1, BoardState o2) {
            return o1.totalMoves - o2.totalMoves;
        }
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardState that = (BoardState) o;
        return board.equals(that.board);
    }

    @Override
    public int hashCode() {
        return board.hashCode();
    }

    public BoardState clone(){
        //TODO: Pull newState from statePool
        BoardState newState = Util.getBoard();
        for(int i = 0; i < board.size(); i++){
            ArrayList<Byte> row = board.get(i);
            //TODO: Pull newRow from rowPool
            ArrayList<Byte> newRow = new ArrayList<>();
            newRow.addAll(row);
            newState.board.add(newRow);
        }
        newState.rowPos = rowPos;
        newState.columnPos = columnPos;
        newState.parent = this;
        return newState;
    }

    public void reset(){
        //TODO recycle Arraylist<Byte>;
        board.clear();
        rowPos = 0;
        columnPos = 0;
        parent = null;
        totalMoves = 0;
        movesFromParent.clear();
    }
}
