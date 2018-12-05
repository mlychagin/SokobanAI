import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.lang.Math;
import java.util.PriorityQueue;

public class GameEngine {
    private LinkedList<BoardState> priorityQue = new LinkedList<>();
    private PriorityQueue<PairBoardState> priorityQueueForHeuristic = new PriorityQueue<>();
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
            ArrayList<Byte> row = Util.getArrayByte();
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
        setDeadPositionsAlgo();
    }

    public void setDeadPositions() {
        Pair p = Util.getPair(0, 0);
        boolean keepGoing = true;
        while (keepGoing) {
            keepGoing = false;
            for (int i = 1; i < board.size() - 1; i++) {
                ArrayList<Byte> row = board.get(i);
                for (int j = 1; j < row.size() - 1; j++) {
                    p.set(i, j);
                    if (row.get(j) == Util.empty && !goalNodes.contains(p)) {
                        if (isDeadLock(row, board.get(i - 1), board.get(i + 1), j)) {
                            row.set(j, Util.deadZone);
                            keepGoing = true;
                        }
                    }
                }
            }
        }
        Util.recycle(p);
    }

    private boolean isDeadLock(ArrayList<Byte> row, ArrayList<Byte> above, ArrayList<Byte> below, int columnIndex) {
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
        ArrayList<BoardState> returnMoves = Util.getArrayBoardState();
        intermediatePriorityQue.add(startState);
        intermediateSeenStates.add(startState);

        BoardState state;
        while (!intermediatePriorityQue.isEmpty()) {
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
        for (BoardState b : intermediateSeenStates) {
            if (!returnMoves.contains(b) && !b.equals(startState)) {
                Util.recycle(b);
            }
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
        while (true) {
            if(priorityQue.size() == 0) return null;
            switch (searchType) {
                //TODO add default case
                case Util.bfs:
                    state = priorityQue.removeFirst();
                    break;
                case Util.dfs:
                    state = priorityQue.removeLast();
                    break;
                case Util.ids:
                    if (depth == depthRequirement) {
                        state = priorityQue.removeFirst();
                        depth = findDepth(state);
                        if (depth == depthRequirement) {
                            depthRequirement = depthRequirement * depthRequirement;
                        }

                    } else {
                        state = priorityQue.removeLast();
                        depth++;

                    }
            }
            ArrayList<BoardState> possibleMoves = findPossibleBoxMoves(state);
            for (int i = 0; i < possibleMoves.size(); i++) {
                BoardState move = possibleMoves.get(i);
                if (isGoalState(move)) {
                    for (int j = i + 1; j < possibleMoves.size(); j++) {
                        Util.recycle(possibleMoves.get(j));
                    }
                    Util.recycleABS(possibleMoves);
                    return move;
                }
                if (!seenStates.contains(move)) {
                    priorityQue.add(move);
                    seenStates.add(move);
                } else {
                    Util.recycle(move);
                }
            }
            Util.recycleABS(possibleMoves);
        }
    }

    private int findDepth(BoardState state) {
        int depth = 0;
        while (state.parent != null) {
            state = state.parent;
            depth++;
        }
        return depth;
    }

    private int findInitDepthRequirement() {
        int lastMax = 0;
        for (ArrayList<Byte> row : board) {
            lastMax = Math.max(row.size(), lastMax);
        }
        return lastMax * lastMax;
    }

    public BoardState findSolutionHelperHeuristic() {
        System.out.print(root.printBoard(board));
        PairBoardState forRoot = new PairBoardState(0, root);
        priorityQueueForHeuristic.add(forRoot);
        seenStates.add(root);


        BoardState state = null;
        int counter = 0;
        while (true) {
            counter++;
            state = priorityQueueForHeuristic.remove().getBoardState();
            ArrayList<BoardState> possibleMoves = findPossibleBoxMoves(state);
            for (int i = 0; i < possibleMoves.size(); i++) {
                BoardState move = possibleMoves.get(i);
                if (isGoalState(move)) {
                    for (int j = i + 1; j < possibleMoves.size(); j++) {
                        Util.recycle(possibleMoves.get(j));
                    }
                    return move;
                }
                if (!seenStates.contains(move)) {
                    int heuristic = 0;
//                    calculate heuristic
                    PairBoardState boardPair = new PairBoardState(heuristic, move);
                    priorityQueueForHeuristic.add(boardPair);
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
        for (ArrayList<Byte> ab : board) {
            Util.recycleAB(ab);
        }
        priorityQue.clear();
        seenStates.clear();
        goalNodes.clear();
        board.clear();
    }

    public ArrayList<Byte> findSolution(int searchType) {
        ArrayList<Byte> returnMoves = Util.getArrayByte();
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

      public void setWallPositionsOutside()
  {
    Pair p = Util.getPair(0, 0);
    for (int i = 0; i < board.size(); i++)
    {
      boolean inside = false;
      Byte last = Util.empty;

      ArrayList<Byte> row = board.get(i);
      for (byte j = 0; j < row.size(); j++)
      {
        p.set(i, j);
        if (row.get(j) != Util.wall && last == Util.wall && !inside)
        {
          last = row.get(j);
          inside = !inside;

        }
        else if(row.get(j) == Util.wall && inside)
        {
          last = row.get(j);

          inside = !inside;
        }
        else if (row.get(j) == Util.empty && !inside)
        {

          last = row.get(j);

          row.set(j, Util.wall);


        }
        else
        {
          last = row.get(j);
        }

      }
    }

    Util.recycle(p);
  }


    public void setDeadPositionsAlgo()
    {
        Pair p = Util.getPair(0, 0);

        for (int i = 1; i < board.size() - 1; i++)
        {
            ArrayList<Byte> row = board.get(i);
            for (int j = 1; j < row.size() - 1; j++)
            {
                p.set(i, j);
                System.out.println("Placeing boxes" + i + "   " + j);
                if (row.get(j) == Util.empty && !goalNodes.contains(p))
                {
                    root.boxPositions.clear();
                    p.set(i,j);
                    root.boxPositions.add(p);
                    System.out.println(root.boxPositions.size());
                    BoardState solutionBoardState = findSolutionHelper(Util.bfs);
                    if (solutionBoardState == null)
                    {
                        System.out.println("Place x");
                        row.set(j, Util.deadZone);
                    }
                }
            }
        }
//    Util.recycle(p);
    }
}
