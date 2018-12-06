import java.util.*;

public class GameEngine {
    private LinkedList<BoardState> priorityQue = new LinkedList<>();
    private PriorityQueue<PairBoardState> priorityQueueForHeuristic = new PriorityQueue<>();
    private HashSet<BoardState> seenStates = new HashSet<>();
    private LinkedList<BoardState> intermediatePriorityQue = new LinkedList<>();
    private HashSet<BoardState> intermediateSeenStates = new HashSet<>();
    private ArrayList<ArrayList<Byte>> board = new ArrayList<>();
    static HashSet<Pair> goalNodes = new HashSet<>();
    private HashSet<Pair> whiteSpaces = new HashSet<>();
    private BoardState root;
    Random rnd = new Random();

    public GameEngine() {
    }

    public void setDeadlocks() {
        System.out.println(root.printBoard(board));
        System.out.flush();
        setDeadPositions();
        setWallPositionsOutside();
        setDeadPositionsAlgo();
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
                        slot = Util.player;
                        break;
                    default:
                        break;
                }
                row.add(slot);
            }
            board.add(row);
        }
        setDeadlocks();
    }

    public void setBoardSize(String line) {
        root = Util.getBoard();
        Scanner word = new Scanner(line);
        int xSize = Integer.parseInt(word.next());
        int ySize = Integer.parseInt(word.next());
        for (int k = 0; k < ySize; k++) {
            ArrayList<Byte> row = new ArrayList<>();
            for (int l = 0; l < xSize; l++) {
                row.add(Util.empty);
            }
            board.add(row);
        }
    }

    public void setWalls(String line) {
        Scanner word = new Scanner(line);
        int nWalls = Integer.parseInt(word.next());
        for (int i = 0; i < nWalls - 1; i++) {
            int xCoor = (Integer.parseInt(word.next()) - 1);
            int yCoor = (Integer.parseInt(word.next()) - 1);
            BoardState.setCoordinate(board, xCoor, yCoor, Util.wall);
        }
    }

    public void setBoxes(String line) {
        Scanner word = new Scanner(line);
        int nBoxes = Integer.parseInt(word.next());
        for (int i = 0; i < nBoxes; i++) {
            int xCoor = (Integer.parseInt(word.next()) - 1);
            int yCoor = (Integer.parseInt(word.next()) - 1);
            root.addBoxLocation(xCoor, yCoor);
        }
    }

    public void setGoals(String line) {
        Scanner word = new Scanner(line);
        int nGoals = Integer.parseInt(word.next());
        for (int i = 0; i < nGoals; i++) {
            int xCoor = (Integer.parseInt(word.next()) - 1);
            int yCoor = (Integer.parseInt(word.next()) - 1);
            goalNodes.add(Util.getPair(xCoor, yCoor));
        }
    }

    public void setSokoban(String line) {
        Scanner word = new Scanner(line);
        int xCoor = (Integer.parseInt(word.next()) - 1);
        int yCoor = (Integer.parseInt(word.next()) - 1);
        root.setPlayerCoordinates(xCoor, yCoor);
        setDeadlocks();
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

    public void findPossibleBoxMoves(BoardState startState, ArrayList<BoardState> returnMoves, HashSet<Pair> visitableVertices) {
        intermediatePriorityQue.add(startState);
        intermediateSeenStates.add(startState);
        if (visitableVertices != null) visitableVertices.add(startState.sokoban.clonePair());

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
                            Pair p = child.sokoban.clonePair();
                            if (visitableVertices != null && !visitableVertices.contains(p)) {
                                visitableVertices.add(p);
                            } else {
                                Util.recycle(p);
                            }
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
        intermediateSeenStates.clear();
    }

    public int calculateHueristic(BoardState boardState, int heuristic){
        return 0;
    }

    public BoardState parseMoves(BoardState state, ArrayList<BoardState> possibleMoves, int searchType, int heuristic){
        for (int i = 0; i < possibleMoves.size(); i++) {
            BoardState move = possibleMoves.get(i);
            if (isGoalState(move)) {
                for (int j = i + 1; j < possibleMoves.size(); j++) {
                    Util.recycle(possibleMoves.get(j));
                }
                return move;
            }
            if (!seenStates.contains(move)) {
                if(searchType == Util.huerisitc){
                    PairBoardState boardPair = new PairBoardState(calculateHueristic(move, heuristic), move);
                    priorityQueueForHeuristic.add(boardPair);
                } else {
                    priorityQue.add(move);
                }
                seenStates.add(move);
            } else {
                Util.recycle(move);
            }
        }
        return null;
    }

    public BoardState nextBoardState(BoardState state, Pair depth, int searchType){
        if (searchType == Util.huerisitc && priorityQueueForHeuristic.isEmpty()) return null;
        if (searchType != Util.huerisitc && priorityQue.isEmpty()) return null;
        switch (searchType) {
            case Util.bfs:
                return priorityQue.removeFirst();
            case Util.dfs:
                return priorityQue.removeLast();
            case Util.ids:
                if (depth.getFirst() == depth.getSecond()) {
                    BoardState returnState = priorityQue.removeFirst();
                    depth.setFirst(findDepth(returnState));
                    if (depth.getFirst() == depth.getSecond()) {
                        depth.setSecond(depth.getSecond() * depth.getSecond());
                    }
                    return returnState;
                } else {
                    depth.setFirst(depth.getFirst() + 1);
                    return priorityQue.removeLast();

                }
            case Util.huerisitc:
                return priorityQueueForHeuristic.remove().getBoardState();
            case Util.random:
                return state;
            default:
                System.out.println("Invalid searchType");
        }
        return null;
    }

    public BoardState initSearch(BoardState startingState, int searchType){
        seenStates.add(startingState);
        switch (searchType) {
            case Util.bfs:
            case Util.dfs:
            case Util.ids:
                priorityQue.add(startingState);
                break;
            case Util.huerisitc:
                priorityQueueForHeuristic.add(new PairBoardState(0, startingState));
                break;
            case Util.random:
                seenStates.remove(startingState);
                return startingState;
            default:
                System.out.println("Invalid searchType");
        }
        return null;
    }

    public BoardState findSolutionHelper(BoardState startingState, int searchType, int heuristic) {
        BoardState state = initSearch(startingState, searchType);
        Pair depth = Util.getPair(0, findInitDepthRequirement());
        while (true) {
            state = nextBoardState(state, depth, searchType);
            if(state == null){
                return null;
            }
            ArrayList<BoardState> possibleMoves = Util.getArrayBoardState();
            findPossibleBoxMoves(state, possibleMoves, null);
            BoardState returnMove = parseMoves(state, possibleMoves, searchType, heuristic);
            Util.recycleABS(possibleMoves);
            if(returnMove != null){
                return returnMove;
            }
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

    public void cleanUpReset() {
        for (BoardState b : priorityQue) {
            seenStates.remove(b);
            Util.recycle(b);
        }
        for (BoardState b : seenStates) {
            Util.recycle(b);
        }
        priorityQue.clear();
        seenStates.clear();
    }

    public void cleanUpAll() {
        cleanUpReset();
        for (Pair p : whiteSpaces) {
            Util.recycle(p);
        }
        for (Pair p : goalNodes) {
            Util.recycle(p);
        }
        for (ArrayList<Byte> ab : board) {
            Util.recycleAB(ab);
        }
        whiteSpaces.clear();
        goalNodes.clear();
        board.clear();
    }

    public ArrayList<Byte> findSolution(int searchType, int heuristic) {
        ArrayList<Byte> returnMoves = Util.getArrayByte();
        BoardState goalState = findSolutionHelper(root, searchType, heuristic);

        BoardState iterState = goalState;
        while (iterState != null) {
            returnMoves.addAll(iterState.movesFromParent);
            iterState = iterState.parent;
        }
        cleanUpAll();
        Util.recycle(goalState);
        return returnMoves;
    }

    private boolean isGoalState(BoardState boardState) {
        for (Pair p : boardState.boxPositions) {
            if (!goalNodes.contains(p)) {
                return false;
            }
        }
        return true;
    }

    private void findWhiteSpaces() {
        BoardState blankState = root.getChild();
        for (Pair p : blankState.boxPositions) {
            Util.recycle(p);
        }
        blankState.boxPositions.clear();
        ArrayList<BoardState> possibleMoves = Util.getArrayBoardState();
        findPossibleBoxMoves(blankState, possibleMoves, whiteSpaces);
        for (BoardState bs : possibleMoves) {
            Util.recycle(bs);
        }
        Util.recycleABS(possibleMoves);
        Util.recycle(blankState);

        ArrayList<Pair> removePairs = new ArrayList<>();
        for (Pair p : whiteSpaces) {
            byte up = BoardState.getCoordinate(board, p.getFirst() + BoardState.getOffsetRow(Util.up), p.getSecond() + BoardState.getOffsetColumn(Util.up));
            byte down = BoardState.getCoordinate(board, p.getFirst() + BoardState.getOffsetRow(Util.down), p.getSecond() + BoardState.getOffsetColumn(Util.down));
            byte left = BoardState.getCoordinate(board, p.getFirst() + BoardState.getOffsetRow(Util.left), p.getSecond() + BoardState.getOffsetColumn(Util.left));
            byte right = BoardState.getCoordinate(board, p.getFirst() + BoardState.getOffsetRow(Util.right), p.getSecond() + BoardState.getOffsetColumn(Util.right));

            int count = 0;
            byte finalDir = 0;
            if (up == Util.wall) {
                count++;
            } else {
                finalDir = Util.up;
            }
            if (down == Util.wall) {
                count++;
            } else {
                finalDir = Util.down;
            }
            if (left == Util.wall) {
                count++;
            } else {
                finalDir = Util.left;
            }
            if (right == Util.wall) {
                count++;
            } else {
                finalDir = Util.right;
            }
            if (count == 3) {
                if (root.sokoban.equals(p)) {
                    root.sokoban.set(p.getFirst() + BoardState.getOffsetRow(finalDir), p.getSecond() + BoardState.getOffsetRow(finalDir));
                    root.movesFromParent.add(finalDir);
                }
                if (!goalNodes.contains(p)) {
                    BoardState.setCoordinate(board, p, Util.wall);
                    removePairs.add(p);
                }
            }
        }
        for (Pair p : removePairs) {
            whiteSpaces.remove(p);
            Util.recycle(p);
        }
    }

    public void setWallPositionsOutside() {
        findWhiteSpaces();
        Pair p = Util.getPair(0, 0);
        for (int i = 0; i < board.size(); i++) {
            ArrayList<Byte> row = board.get(i);
            for (int j = 0; j < row.size(); j++) {
                p.set(i, j);
                if (!whiteSpaces.contains(p)) {
                    row.set(j, Util.wall);
                }
            }
        }
        Util.recycle(p);
    }

    public void setDeadPositionsAlgo() {
        BoardState iterState = root.getChild();
        for (Pair p : iterState.boxPositions) {
            Util.recycle(p);
        }
        iterState.boxPositions.clear();
        for (Pair p : whiteSpaces) {
            if (BoardState.getCoordinate(board, p) != Util.deadZone && !goalNodes.contains(p)) {
                iterState.boxPositions.add(p);
                BoardState solutionBoardState = findSolutionHelper(iterState, Util.dfs, Util.hBoxesOnGoal);
                if (solutionBoardState == null) {
                    BoardState.setCoordinate(board, p, Util.deadZone);
                } else {
                    Util.recycle(solutionBoardState);
                }
                seenStates.remove(iterState);
                cleanUpReset();
                iterState.boxPositions.clear();
            }
        }
        Util.recycle(iterState);
    }

    public int hblocksOnGoal(BoardState state)
    {
      int counter = 0;
      for (Pair p : state.boxPositions) {
        if (goalNodes.contains(p)) {
          counter ++;
        }
      }
      return counter;
    }
    public int hManhattanToAnyGoal(BoardState state)
    {
      int totalDistance = 0;
      int tempDistance = -1;
      for(Pair p : state.boxPositions)
      {
        for(Pair g: goalNodes)
        {
          tempDistance = Math.max(tempDistance, manhattanDistance(p,g));
        }
        totalDistance += tempDistance;
        tempDistance = -1;
      }
      return totalDistance;
    }
  public int hManhattanToSingleGoal(BoardState state)
  {
    int totalDistance = 0;
    int tempDistance = -1;
    Pair tempGoalPair = new Pair(-1,-1);
    HashSet<Pair> tempGoalNodes = (HashSet)goalNodes.clone();
    for(Pair p : state.boxPositions)
    {
      for(Pair g: tempGoalNodes)
      {
        int dis = manhattanDistance(p,g);
        if (Math.max(tempDistance,dis)  == dis)
        {
          tempDistance = dis;
          tempGoalPair = g;
        }

      }
      totalDistance += tempDistance;
      tempGoalNodes.remove(tempGoalPair);
      tempDistance = -1;
    }
    return totalDistance;
  }
    private int manhattanDistance(Pair source, Pair destination)
    {
      return Math.abs(source.first - destination.first) + Math.abs(source.second - destination.second);
    }
  private int euclideanDistanceSquared(Pair source, Pair destination)
  {
    int temp1 = (destination.first - source.first) * (destination.first - source.first);
    int temp2 = (destination.second - source.second) * (destination.second - source.second);
    return temp1 + temp2;
  }

  public int minMatching(BoardState state)
  {
    int i = 0;
    int j = 0;
    int[][] cost = new int[state.boxPositions.size()][goalNodes.size()];
    ArrayList<TreeSet<Pair>> priority= new ArrayList<>();
    HashMap<Integer,Integer> goalToBox = new HashMap<Integer, Integer>();
    for(Pair p : state.boxPositions)
    {
      TreeSet<Pair> boxCosts = new TreeSet<>();
      for(Pair g: goalNodes)
      {
//        Calculate real cost
        cost[i][j] = manhattanDistance(p,g);
        boxCosts.add(new Pair(cost[i][j],j));

        j++;
      }
      priority.add(boxCosts);
      j = 0;
      i++;
    }
//    Take the array and find the min matching cost
    for(int k = 0; k < priority.size(); k++)
    {

    }
    return 0;
  }
  private void resolveConflicts(ArrayList<TreeSet<Pair>> priority, HashMap<Integer,Integer> goalToBox,int boxNum)
  {
    TreeSet<Pair> currentBoxSet = priority.get(boxNum);
    Pair currentMinPair = currentBoxSet.first();
    if(goalToBox.containsKey(currentMinPair.second))
    {
       int conflictBoxNum = goalToBox.get(currentMinPair.second);
       TreeSet<Pair> conflictBoxSet = priority.get(conflictBoxNum);
       Pair conflictMinPair = conflictBoxSet.first();

       Iterator<Pair> currentIter = currentBoxSet.iterator();
       Iterator<Pair> conflictIter = conflictBoxSet.iterator();
        Pair currentNextPair = null;
        Pair conflictNext = null;
       for(int i= 0;i < 2; i++)
       {

          if(currentIter.hasNext())
          {
            currentNextPair= currentIter.next();
          }
          else{
            currentNextPair = null;
          }
         if(conflictIter.hasNext())
         {
           conflictNext = conflictIter.next();
         }
         else
         {
           conflictNext = null;

         }
       }
       if(currentNextPair == null) {
         goalToBox.replace(currentMinPair.second,boxNum);
         conflictBoxSet.pollFirst();
         resolveConflicts(priority,goalToBox,conflictBoxNum);
         return;
       }
       else if(conflictNext == null) {
         currentBoxSet.pollFirst();
         resolveConflicts(priority,goalToBox,boxNum);
         return;

       }
       int currentDifferenceInValue = currentNextPair.first - currentMinPair.first;
       int conflictDifferenceInValue = conflictNext.first - conflictMinPair.first;

       if(currentDifferenceInValue > conflictDifferenceInValue)
       {
         currentBoxSet.pollFirst();
         resolveConflicts(priority,goalToBox,boxNum);
         return;
       }
       else
       {
         goalToBox.replace(currentMinPair.second,boxNum);
         conflictBoxSet.pollFirst();
         resolveConflicts(priority,goalToBox,conflictBoxNum);
         return;
       }

    }
    goalToBox.put(currentMinPair.second,boxNum);
  }
}
