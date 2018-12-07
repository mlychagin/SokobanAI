import java.util.ArrayList;

public class BoardState implements Comparable<BoardState> {
    ArrayList<Pair> boxPositions = new ArrayList<>();
    Pair sokoban = new Pair(0,0);

    int hueristicValue = 0;
    BoardState parent = null;
    ArrayList<Byte> movesFromParent = new ArrayList<>();

    public BoardState() {
    }

    public void setPlayerCoordinates(int x, int y) {
        sokoban.setFirst(x);
        sokoban.setSecond(y);
    }

    void addBoxLocation(int x, int y) {
        boxPositions.add(Util.getPair(x, y));
    }

    void addBoxLocation(Pair p) {
        boxPositions.add(p);
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

    private void loadBoard(ArrayList<ArrayList<Byte>> board) {
        for (Pair location : GameEngine.goalNodes.keySet()) {
            Util.setCoordinate(board, location, Util.goal);
        }
        for (Pair location : boxPositions) {
            Util.setCoordinate(board, location, Util.getCoordinate(board, location) == Util.goal ? Util.boxOnGoal : Util.box);
        }
    }

    private void resetBoard(ArrayList<ArrayList<Byte>> board) {
        for (Pair location : boxPositions) {
            Util.setCoordinate(board, location, Util.empty);
        }
        for (Pair location : GameEngine.goalNodes.keySet()) {
            Util.setCoordinate(board, location, Util.empty);
        }
    }

    private void updateBoxPosition(Pair startLocation, Pair endLocation) {
        for (Pair pair : boxPositions) {
            if (pair.equals(startLocation)) {
                pair.set(endLocation);
            }
        }
    }

    private void moveBoxCleanUp(ArrayList<ArrayList<Byte>> board, Pair startLocation, Pair endLocation, byte direction) {
        updateBoxPosition(startLocation, endLocation);
        Util.setCoordinate(board, startLocation, Util.getCoordinate(board, startLocation) == Util.box ? Util.empty : Util.goal);
        Util.setCoordinate(board, endLocation, Util.getCoordinate(board, endLocation) == Util.empty ? Util.box : Util.boxOnGoal);
        updatePlayerPositionAfterMoving(direction);
        moveBoxExtra(board, endLocation, direction);
    }

    private void moveBoxExtra(ArrayList<ArrayList<Byte>> board, Pair startLocation, byte direction) {
        Pair endLocation = Util.getPair(startLocation.getFirst() + Util.getOffsetRow(direction), startLocation.getSecond() + Util.getOffsetColumn(direction));
        byte endLocationValue = Util.getCoordinate(board, endLocation);
        if (endLocationValue == Util.wall || endLocationValue == Util.deadZone || !Util.moveBoxExtraHelper(board, startLocation, endLocation, direction)) {
            Util.recycle(endLocation);
            return;
        }
        moveBoxCleanUp(board, startLocation, endLocation, direction);
        Util.recycle(endLocation);
    }

    private boolean moveBox(ArrayList<ArrayList<Byte>> board, Pair startLocation, byte direction) {
        Pair endLocation = Util.getPair(startLocation.getFirst() + Util.getOffsetRow(direction), startLocation.getSecond() + Util.getOffsetColumn(direction));
        byte endLocationValue = Util.getCoordinate(board, endLocation);
        if (!(endLocationValue == Util.empty || endLocationValue == Util.goal)) {
            Util.recycle(endLocation);
            return false;
        }
        moveBoxCleanUp(board, startLocation, endLocation, direction);
        Util.recycle(endLocation);
        return true;
    }

    public PairPairByte move(ArrayList<ArrayList<Byte>> board, byte direction) {
        PairPairByte result = Util.getPairPairByte();
        loadBoard(board);
        int offsetRow = Util.getOffsetRow(direction);
        int offsetColumn = Util.getOffsetColumn(direction);
        result.returnType = Util.invalidMove;
        Pair location = Util.getPair(sokoban.getFirst() + offsetRow, sokoban.getSecond() + offsetColumn);
        switch (Util.getCoordinate(board, location)) {
            case Util.box:
            case Util.boxOnGoal:
                result.returnType = moveBox(board, location, direction) ? Util.boxMove : Util.invalidBoxMove;
                break;
            case Util.deadZone:
            case Util.empty:
            case Util.goal:
                updatePlayerPositionAfterMoving(direction);
                result.returnType = Util.playerMove;
                break;
            default:
                break;
        }
        resetBoard(board);
        if (result.returnType != Util.invalidMove && result.returnType != Util.invalidBoxMove) {
            movesFromParent.add(direction);
        }
        Util.recycle(location);
        return result;
    }

    public void loadPlayer(ArrayList<ArrayList<Byte>> board) {
        switch (Util.getCoordinate(board, sokoban)) {
            case Util.empty:
                Util.setCoordinate(board, sokoban, Util.player);
                break;
            case Util.goal:
                Util.setCoordinate(board, sokoban, Util.playerOnGoal);
                break;
            case Util.deadZone:
                Util.setCoordinate(board, sokoban, Util.playerOnDeadZone);
                break;
            default:
                System.out.println("Invalid Player Load");
                break;
        }
    }

    public void resetPlayer(ArrayList<ArrayList<Byte>> board) {
        switch (Util.getCoordinate(board, sokoban)) {
            case Util.player:
                Util.setCoordinate(board, sokoban, Util.empty);
                break;
            case Util.playerOnGoal:
                Util.setCoordinate(board, sokoban, Util.goal);
                break;
            case Util.playerOnDeadZone:
                Util.setCoordinate(board, sokoban, Util.deadZone);
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
        newState.hueristicValue = 0;
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
        hueristicValue = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardState that = (BoardState) o;
        for (Pair p : boxPositions) {
            if (!that.boxPositions.contains(p)) {
                return false;
            }
        }
        return sokoban.equals(that.sokoban);
    }

    @Override
    public int hashCode() {
        int result = boxPositions.hashCode();
        result = 31 * result + sokoban.hashCode();
        return result;
    }

    @Override
    public int compareTo(BoardState o) {
        return Integer.compare(this.hueristicValue, o.hueristicValue);
    }
}
