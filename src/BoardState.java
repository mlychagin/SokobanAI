import java.util.ArrayList;
import java.util.HashSet;

public class BoardState{
    ArrayList<Pair> boxPositions = new ArrayList<>();
    Pair sokoban = Util.getPair(0,0);

    static BoardDimension boardDim;
    BoardState parent = null;
    ArrayList<Byte> movesFromParent = new ArrayList<>();

    public class BoardDimension{
    	
    	Pair  boardDimension = Util.getPair(0, 0);
    	
    	Pair setBoardDimensions(int x, int y){
        	boardDimension = Util.getPair(x, y);
    		return boardDimension;
        }
        
        Pair getBoardDimensions(){
    		return boardDimension;	
        }
    }
    
    public BoardState(){
    }

    
    public void setPlayerCoordinates(int x, int y){
        sokoban.setFirst(x);
        sokoban.setSecond(y);
    }

    void addBoxLocation(int x, int y){
        boxPositions.add(Util.getPair(x,y));
    }

    private void updatePlayerPositionAfterMoving(byte direction){
        switch (direction){
            case Util.up:
                sokoban.first--;
                break;
            case Util.down:
                sokoban.first++;
                break;
            case Util.right:
                sokoban.second++;
                break;
            case Util.left:
                sokoban.second--;
                break;
        }
    }

    private void setCoordinate(ArrayList<ArrayList<Byte>> board, Pair location, byte slot){
        board.get(location.getFirst()).set(location.getSecond(), slot);
    }

    private byte getCoordinate(ArrayList<ArrayList<Byte>> board, Pair location){
        return board.get(location.getFirst()).get(location.getSecond());
    }

    private void loadBoard(ArrayList<ArrayList<Byte>> board){
        for(Pair location : boxPositions){
            setCoordinate(board, location, GameEngine.goalNodes.contains(location) ? Util.boxOnGoal : Util.box);
        }
    }

    private void resetBoard(ArrayList<ArrayList<Byte>> board){
        for(Pair location : boxPositions){
            setCoordinate(board, location, GameEngine.goalNodes.contains(location) ? Util.goal : Util.empty);
        }
    }

    private boolean moveBox(ArrayList<ArrayList<Byte>> board, Pair startLocation, Pair endLocation){
        switch(getCoordinate(board, endLocation)){
            case Util.empty:
            case Util.goal:
                for(Pair pair : boxPositions){
                    if(pair.equals(startLocation)){
                        pair.set(endLocation);
                    }
                }
                break;
            default:
                return false;
        }
        setCoordinate(board, startLocation, getCoordinate(board, startLocation) == Util.box ? Util.empty : Util.goal);
        return true;
    }

    public byte move(ArrayList<ArrayList<Byte>> board, byte direction){
        loadBoard(board);
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
        byte returnValue = Util.invalidMove;
        Pair location = Util.getPair(sokoban.getFirst() + offsetRow, sokoban.getSecond() + offsetColumn);
        switch (getCoordinate(board, location)){
            case Util.box:
            case Util.boxOnGoal:
                if(moveBox(board, location, Util.getPair(location.getFirst() + offsetRow, location.getSecond() + offsetColumn))){
                    updatePlayerPositionAfterMoving(direction);
                    returnValue = Util.boxMove;
                }
                break;
            case Util.empty:
            case Util.goal:
                updatePlayerPositionAfterMoving(direction);
                returnValue =  Util.playerMove;
                break;
            default:
        }
        resetBoard(board);
        if(returnValue != Util.invalidMove){
            movesFromParent.add(direction);
        }
        return returnValue;
    }

    public void loadPlayer(ArrayList<ArrayList<Byte>> board){
        switch (getCoordinate(board, sokoban)){
            case Util.empty:
                setCoordinate(board, sokoban, Util.player);
                break;
            case Util.goal:
                setCoordinate(board, sokoban, Util.playerOnGoal);
                break;
            default:
                break;
        }
    }

    public void resetPlayer(ArrayList<ArrayList<Byte>> board){
        switch (getCoordinate(board, sokoban)){
            case Util.player:
                setCoordinate(board, sokoban, Util.empty);
                break;
            case Util.playerOnGoal:
                setCoordinate(board, sokoban, Util.goal);
                break;
            default:
                break;
        }
    }

    public String printBoardInternal(ArrayList<ArrayList<Byte>> board){
        StringBuilder builder = new StringBuilder();
        for (ArrayList<Byte> row : board) {
            for (byte column : row) {
                builder.append((char)column).append(" ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    public String printBoard(ArrayList<ArrayList<Byte>> board){
        loadBoard(board);
        loadPlayer(board);
        StringBuilder builder = new StringBuilder();
        for (ArrayList<Byte> row : board) {
            for (byte column : row) {
                builder.append((char)column).append(" ");
            }
            builder.append("\n");
        }
        resetPlayer(board);
        resetBoard(board);
        return builder.toString();
    }

    public BoardState getChild(){
        BoardState newState = Util.getBoard();
        for(Pair p : boxPositions){
            newState.boxPositions.add(p.clonePair());
        }
        newState.sokoban.set(this.sokoban);
        newState.parent = this;
        return newState;
    }

    public void reset(){
        for(Pair p : boxPositions){
            Util.recycle(p);
        }
        boxPositions.clear();
        parent = null;
        movesFromParent.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardState that = (BoardState) o;
        if (!boxPositions.equals(that.boxPositions)) return false;
        return sokoban.equals(that.sokoban);
    }

    @Override
    public int hashCode() {
        int result = boxPositions.hashCode();
        result = 31 * result + sokoban.hashCode();
        return result;
    }
}
