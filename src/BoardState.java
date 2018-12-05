import java.util.ArrayList;
import java.util.HashSet;

public class BoardState {
    ArrayList<Pair> boxPositions = new ArrayList<>();
    Pair sokoban = Util.getPair(0, 0);

    BoardState parent = null;
    ArrayList<Byte> movesFromParent = Util.getArrayByte();

    public BoardState() {
    }

    public void setPlayerCoordinates(int x, int y) {
        sokoban.setFirst(x);
        sokoban.setSecond(y);
    }

    void addBoxLocation(int x, int y) {
        boxPositions.add(Util.getPair(x, y));
    }

    private void updatePlayerPositionAfterMoving(byte direction) {
        switch (direction) {
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

    public static void setCoordinate(ArrayList<ArrayList<Byte>> board, Pair location, byte slot) {
        board.get(location.getFirst()).set(location.getSecond(), slot);
    }

    public static byte getCoordinate(ArrayList<ArrayList<Byte>> board, Pair location) {
        return board.get(location.getFirst()).get(location.getSecond());
    }

    public static byte getCoordinate(ArrayList<ArrayList<Byte>> board, int x, int y) {
        return board.get(x).get(y);
    }

    public static byte getCoordinate(ArrayList<ArrayList<Byte>> board,  Pair location, int offsetRow, int offsetColumn) {
        return board.get(location.getFirst() + offsetRow).get(location.getSecond() + offsetColumn);
    }

    private void loadBoard(ArrayList<ArrayList<Byte>> board) {
        for(Pair location : GameEngine.goalNodes){
            setCoordinate(board, location, Util.goal);
        }
        for (Pair location : boxPositions) {
            setCoordinate(board, location, getCoordinate(board, location) == Util.goal ? Util.boxOnGoal : Util.box);
        }
    }

    private void resetBoard(ArrayList<ArrayList<Byte>> board) {
        for (Pair location : boxPositions) {
            setCoordinate(board, location, Util.empty);
        }
        for (Pair location : GameEngine.goalNodes) {
            setCoordinate(board, location, Util.empty);
        }
    }

    private boolean moveBoxExtraHelper(ArrayList<ArrayList<Byte>> board, Pair startLocation, Pair endLocation, byte direction){
        byte ud1 = getCoordinate(board, startLocation, 0, 1);
        byte ud2 = getCoordinate(board, startLocation, 0, -1);
        byte ud3 = getCoordinate(board, endLocation, 0 ,1);
        byte ud4 = getCoordinate(board, endLocation, 0, -1);

        byte lr1 = getCoordinate(board, startLocation, 1, 0);
        byte lr2 = getCoordinate(board, startLocation, -1, 0);
        byte lr3 = getCoordinate(board, endLocation, 1 ,0);
        byte lr4 = getCoordinate(board, endLocation, -1, 0);

        switch (direction) {
            case Util.up:
            case Util.down:
                if (!(ud1 == Util.wall && ud2 == Util.wall && ud3 == Util.wall && ud4 == Util.wall)) {
                    return false;
                }
                break;
            case Util.left:
            case Util.right:
                if (!(lr1 == Util.wall && lr2 == Util.wall && lr3 == Util.wall && lr4 == Util.wall)) {
                    return false;
                }
                break;
        }
        return true;
    }

    private void updateBoxPosition(Pair startLocation, Pair endLocation){
        for (Pair pair : boxPositions) {
            if (pair.equals(startLocation)) {
                pair.set(endLocation);
            }
        }
    }

    public static int getOffsetRow(byte direction){
        switch (direction) {
            case Util.up:
                return -1;
            case Util.down:
                return 1;
            case Util.right:
            case Util.left:
                return 0;
            default:
                System.out.println("Incorrect Direction");
        }
        return 0;
    }

    public static int getOffsetColumn(byte direction){
        switch (direction) {
            case Util.up:
            case Util.down:
                return 0;
            case Util.right:
                return 1;
            case Util.left:
                return -1;
            default:
                System.out.println("Incorrect Direction");
        }
        return 0;
    }

    private void moveBoxCleanUp(ArrayList<ArrayList<Byte>> board, Pair startLocation, Pair endLocation, byte direction){
        updateBoxPosition(startLocation, endLocation);
        setCoordinate(board, startLocation, getCoordinate(board, startLocation) == Util.box ? Util.empty : Util.goal);
        setCoordinate(board, endLocation, getCoordinate(board, endLocation) == Util.empty ? Util.box : Util.boxOnGoal);
        updatePlayerPositionAfterMoving(direction);
        moveBoxExtra(board, endLocation, direction);
    }

    private void moveBoxExtra(ArrayList<ArrayList<Byte>> board, Pair startLocation, byte direction) {
        Pair endLocation = Util.getPair(startLocation.getFirst() + getOffsetRow(direction), startLocation.getSecond() + getOffsetColumn(direction));
        byte endLocationValue = getCoordinate(board, endLocation);
        if(endLocationValue == Util.wall || endLocationValue == Util.deadZone || !moveBoxExtraHelper(board, startLocation, endLocation, direction)){
            Util.recycle(endLocation);
            return;
        }
        moveBoxCleanUp(board, startLocation, endLocation, direction);
        Util.recycle(endLocation);
    }

    private boolean moveBox(ArrayList<ArrayList<Byte>> board, Pair startLocation, byte direction) {
        Pair endLocation = Util.getPair(startLocation.getFirst() + getOffsetRow(direction), startLocation.getSecond() + getOffsetColumn(direction));
        byte endLocationValue = getCoordinate(board, endLocation);
        if(!(endLocationValue == Util.empty || endLocationValue == Util.goal)){
            Util.recycle(endLocation);
            return false;
        }
        moveBoxCleanUp(board, startLocation, endLocation, direction);
        Util.recycle(endLocation);
        return true;
    }

    public byte move(ArrayList<ArrayList<Byte>> board, byte direction) {
        loadBoard(board);
        int offsetRow = getOffsetRow(direction);
        int offsetColumn = getOffsetColumn(direction);
        byte returnValue = Util.invalidMove;
        Pair location = Util.getPair(sokoban.getFirst() + offsetRow, sokoban.getSecond() + offsetColumn);
        switch (getCoordinate(board, location)) {
            case Util.box:
            case Util.boxOnGoal:
                if (moveBox(board, location, direction)) {
                    returnValue = Util.boxMove;
                }
                break;
            case Util.deadZone:
            case Util.empty:
            case Util.goal:
                updatePlayerPositionAfterMoving(direction);
                returnValue = Util.playerMove;
                break;
            default:
                break;
        }
        resetBoard(board);
        if (returnValue != Util.invalidMove) {
            movesFromParent.add(direction);
        }
        Util.recycle(location);
        return returnValue;
    }

    public void loadPlayer(ArrayList<ArrayList<Byte>> board) {
        switch (getCoordinate(board, sokoban)) {
            case Util.empty:
                setCoordinate(board, sokoban, Util.player);
                break;
            case Util.goal:
                setCoordinate(board, sokoban, Util.playerOnGoal);
                break;
            case Util.deadZone:
                setCoordinate(board, sokoban, Util.playerOnDeadZone);
                break;
            default:
                System.out.println("Invalid Player Load");
                break;
        }
    }

    public void resetPlayer(ArrayList<ArrayList<Byte>> board) {
        switch (getCoordinate(board, sokoban)) {
            case Util.player:
                setCoordinate(board, sokoban, Util.empty);
                break;
            case Util.playerOnGoal:
                setCoordinate(board, sokoban, Util.goal);
                break;
            case Util.playerOnDeadZone:
                setCoordinate(board, sokoban, Util.deadZone);
                break;
            default:
                System.out.println("Invalid Player Reset");
                break;
        }
    }

    public String printBoardInternal(ArrayList<ArrayList<Byte>> board) {
        StringBuilder builder = new StringBuilder();
        loadPlayer(board);
        for (ArrayList<Byte> row : board) {
            for (byte column : row) {
                builder.append((char) column).append(" ");
            }
            builder.append("\n");
        }
        resetPlayer(board);
        return builder.toString();
    }

    public String printBoard(ArrayList<ArrayList<Byte>> board) {
        loadBoard(board);
        loadPlayer(board);
        StringBuilder builder = new StringBuilder();
        for (ArrayList<Byte> row : board) {
            for (byte column : row) {
                builder.append((char) column).append(" ");
            }
            builder.append("\n");
        }
        resetPlayer(board);
        resetBoard(board);
        return builder.toString();
    }

    public BoardState getChild() {
        BoardState newState = Util.getBoard();
        for (Pair p : boxPositions) {
            newState.boxPositions.add(p.clonePair());
        }
        newState.sokoban.set(this.sokoban);
        newState.parent = this;
        return newState;
    }

    public void reset() {
        for (Pair p : boxPositions) {
            Util.recycle(p);
        }
        parent = null;
        boxPositions.clear();
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
