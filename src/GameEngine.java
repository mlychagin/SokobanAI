import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.lang.Math;

public class GameEngine {
    private LinkedList<BoardState> priorityQue = new LinkedList<>();
    private HashSet<BoardState> seenStates = new HashSet<>();
    private LinkedList<BoardState> intermediatePriorityQue = new LinkedList<>();
    private HashSet<BoardState> intermediateSeenStates = new HashSet<>();
    private ArrayList<ArrayList<Byte>> board = new ArrayList<>();
    static HashSet<Pair> goalNodes = new HashSet<>();
    private BoardState root;

    public GameEngine() {
    }

    public void initBoard(ArrayList<String> map) {
        root = Util.getBoard();
        for (int i = 0; i < map.size(); i++) {
            String s = map.get(i);
            ArrayList<Byte> row = new ArrayList<>();
            for (int j = 0; j < s.length(); j++) {
                byte slot = (byte) s.charAt(j);
                switch (slot) {
                    case Util.box:
                        root.addBoxLocation(i, j);
                        slot = Util.empty;
                        break;
                    case Util.goal:
                        goalNodes.add(Util.getPair(i, j));
                        break;
                    case Util.boxOnGoal:
                        root.addBoxLocation(i, j);
                        goalNodes.add(Util.getPair(i, j));
                        slot = Util.goal;
                        break;
                    case Util.player:
                        root.setPlayerCoordinates(i, j);
                        slot = Util.empty;
                        break;
                    case Util.playerOnGoal:
                        goalNodes.add(Util.getPair(i, j));
                        root.setPlayerCoordinates(i, j);
                        slot = Util.goal;
                        break;
                    default:
                        break;
                }
                row.add(slot);
            }
            board.add(row);
        }
        setDeadPositions();
    }

    public void setDeadPositions() {
        Pair p = Util.getPair(0,0);
        boolean keepGoing = true;
        while (keepGoing) {
            keepGoing = false;
            for (int i = 1; i < board.size() - 1; i++) {
                ArrayList<Byte> row = board.get(i);
                for (int j = 1; j < row.size() - 1; j++) {
                    p.set(i,j);
                    if (row.get(j) == Util.empty && !goalNodes.contains(p)) {
                        if (isDeadLock(row, board.get(i - 1), board.get(i + 1), i, j)) {
                            row.set(j, Util.deadZone);
                            keepGoing = true;
                        }
                    }
                }
            }
        }
        Util.recycle(p);
    }


    private boolean isDeadLock(ArrayList<Byte> row, ArrayList<Byte> above, ArrayList<Byte> below, int rowIndex, int columnIndex) {
        int totalMoves = 0;
        Byte aboveByte = columnIndex >= above.size() ? Util.wall : above.get(columnIndex);
        Byte belowByte = columnIndex >= below.size() ? Util.wall : below.get(columnIndex);
        if (moveAble(aboveByte, belowByte)) totalMoves++;
        if (moveAble(belowByte, aboveByte)) totalMoves++;
        if (moveAble(row.get(columnIndex + 1), row.get(columnIndex - 1))) totalMoves++;
        if (moveAble(row.get(columnIndex - 1), row.get(columnIndex + 1))) totalMoves++;
        return totalMoves < 1;
    }

    private boolean moveAble(Byte sokoban, Byte destinationOfBlock) {
        return destinationOfBlock != Util.deadZone && destinationOfBlock != Util.wall && sokoban != Util.wall;
    }

    public ArrayList<BoardState> findPossibleBoxMoves(BoardState startState) {
        ArrayList<BoardState> returnMoves = new ArrayList<>();
        intermediatePriorityQue.add(startState);
        intermediateSeenStates.add(startState);

        BoardState state;
        while (!intermediatePriorityQue.isEmpty()) {
            if (Util.getBoardStateCount() != Util.getBoardStateSize() + seenStates.size() + intermediateSeenStates.size() - 1) {
                System.out.println("Leak in findPossibleBoxMoves1");
            }
            state = intermediatePriorityQue.removeFirst();
            BoardState child = state.getChild();
            for (byte i = Util.up; i <= Util.down; i++) {
                byte moveType = child.move(board, i);
                if (moveType == Util.invalidMove) {
                    continue;
                }
                if (!intermediateSeenStates.contains(child)) {
                    switch (moveType) {
                        case Util.playerMove:
                            intermediatePriorityQue.add(child);
                            intermediateSeenStates.add(child);
                            break;
                        case Util.boxMove:
                            returnMoves.add(child);
                            intermediateSeenStates.add(child);
                            ArrayList<Byte> childMovesFromParent = child.movesFromParent;
                            child = child.parent;
                            while (!child.equals(startState)) {
                                childMovesFromParent.addAll(child.movesFromParent);
                                child = child.parent;
                            }
                            returnMoves.get(returnMoves.size() - 1).parent = startState;
                            break;
                        default:
                            System.out.println("Incorrect Move");
                            break;

                    }
                } else {
                    Util.recycle(child);
                }
                child = state.getChild();
            }
            Util.recycle(child);
        }
        int beforeSize = Util.getBoardStateSize();
        for (BoardState b : intermediateSeenStates) {
            if (!returnMoves.contains(b) && !b.equals(startState)) {
                Util.recycle(b);
            }
        }
        if (Util.getBoardStateSize() - beforeSize != intermediateSeenStates.size() - returnMoves.size() - 1) {
            System.out.println("Leak in findPossibleBoxMoves2");
        }
        //TODO Pool Arraylist<BoardStates>
        intermediateSeenStates.clear();
        return returnMoves;
    }

    public BoardState findSolutionHelper(int searchType) {
        System.out.print(root.printBoard(board));
        priorityQue.add(root);
        seenStates.add(root);
        int depth = 0;
        int depthRequirement = findInitDepthRequirement();

        BoardState state = null;
        int counter =0;
        while (true) {
            counter ++;
            switch (searchType) {
                //TODO add default case
                case Util.bfs:
                    state = priorityQue.removeFirst();
                    break;
                case Util.dfs:
                    state = priorityQue.removeLast();
                    break;

                case Util.bAbs:
                    if(depth == depthRequirement)
                    {
                        state = priorityQue.removeFirst();
                        depth = findDepth(state);
                        if(depth == depthRequirement)
                        {
                            depthRequirement = depthRequirement * depthRequirement;
                        }

                    }
                    else
                    {
                        state = priorityQue.removeLast();
                        depth ++;

                    }
            }
            ArrayList<BoardState> possibleMoves = findPossibleBoxMoves(state);
            if (Util.getBoardStateCount() != Util.getBoardStateSize() + possibleMoves.size() + seenStates.size()) {
                System.out.println("Leak in findSolutionBFSHelper");
            }
            for (int i = 0; i < possibleMoves.size(); i++) {
                BoardState move = possibleMoves.get(i);
                if (isGoalState(move)) {
                    for (int j = i + 1; j < possibleMoves.size(); j++) {
                        Util.recycle(possibleMoves.get(j));
                    }
                    return move;
                }
                if (!seenStates.contains(move)) {
                    priorityQue.add(move);
                    seenStates.add(move);
                } else {
                    Util.recycle(move);
                }
            }
        }
    }
    private int findDepth(BoardState state)
    {
        int depth = 0;

        while(state.parent != null)
        {
            depth ++;
            state = state.parent;
        }
        return depth;
    }
    private int findInitDepthRequirement()
    {
        int lastMax = 0;
        for(int i = 0; i < board.size(); i++)
        {
            ArrayList<Byte>row = board.get(i);
            lastMax = Math.max(row.size(),lastMax);
        }
        return lastMax * lastMax;
    }
    public BoardState findSolutionHelperIterative() {
        System.out.print(root.printBoard(board));
        priorityQue.add(root);
        seenStates.add(root);


        BoardState state = null;
        int counter =0;
        while (true) {
            counter ++;

            ArrayList<BoardState> possibleMoves = findPossibleBoxMoves(state);
            if (Util.getBoardStateCount() != Util.getBoardStateSize() + possibleMoves.size() + seenStates.size()) {
                System.out.println("Leak in findSolutionBFSHelper");
            }
            for (int i = 0; i < possibleMoves.size(); i++) {
                BoardState move = possibleMoves.get(i);
                if (isGoalState(move)) {
                    for (int j = i + 1; j < possibleMoves.size(); j++) {
                        Util.recycle(possibleMoves.get(j));
                    }
                    return move;
                }
                if (!seenStates.contains(move)) {
                    priorityQue.add(move);
                    seenStates.add(move);
                } else {
                    Util.recycle(move);
                }
            }
        }
    }
    public void cleanUp() {
        for (BoardState b : priorityQue) {
            seenStates.remove(b);
            Util.recycle(b);
        }
        for (BoardState b : seenStates) {
            Util.recycle(b);
        }
        for (Pair p : goalNodes) {
            Util.recycle(p);

        }
        priorityQue.clear();
        seenStates.clear();
        goalNodes.clear();
        board.clear();
    }

    public ArrayList<Byte> findSolution(int searchType) {
        ArrayList<Byte> returnMoves = new ArrayList<>();
        BoardState goalState = findSolutionHelper(searchType);

        BoardState iterState = goalState;
        while (iterState != null) {
            returnMoves.addAll(iterState.movesFromParent);
            iterState = iterState.parent;
        }
        cleanUp();
        Util.recycle(goalState);
        return returnMoves;
    }

    private boolean isGoalState(BoardState boardState) {
        for (Pair p : boardState.boxPositions) {
            if (!goalNodes.contains(p)) {
                return false;
            }
        }
        System.out.println("Finished");
        return true;
    }
}
