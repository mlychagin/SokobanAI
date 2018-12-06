import java.util.*;

public class GameEngine {
    private HashSet<BoardState> seenStates = new HashSet<>();
    private PriorityQueue<PairBoardState> pqH = new PriorityQueue<>();
    private LinkedList<BoardState> pq = new LinkedList<>();
    private LinkedList<BoardState> intpq = new LinkedList<>();
    private HashSet<BoardState> intSeenStates = new HashSet<>();
    HashMap<DoublePair, ArrayList<Integer>> distances = new HashMap<>();
    private ArrayList<ArrayList<Byte>> board = new ArrayList<>();
    static HashMap<Pair, Integer> goalNodes = new HashMap<>();
    private HashSet<Pair> whiteSpaces = new HashSet<>();
    private BoardState root;
    private Random rnd = new Random();
    Util util = new Util();

    public GameEngine() {
    }

    /*
     * Initialization
     */

    public void initBoard(ArrayList<String> map) {
        int goalCount = 0;
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
                        goalNodes.put(Util.getPair(i, j), goalCount);
                        goalCount++;
                        break;
                    case Util.boxOnGoal:
                        root.addBoxLocation(i, j);
                        goalNodes.put(Util.getPair(i, j), goalCount);
                        slot = Util.goal;
                        goalCount++;
                        break;
                    case Util.player:
                        root.setPlayerCoordinates(i, j);
                        slot = Util.empty;
                        break;
                    case Util.playerOnGoal:
                        goalNodes.put(Util.getPair(i, j), goalCount);
                        root.setPlayerCoordinates(i, j);
                        slot = Util.player;
                        goalCount++;
                        break;
                    default:
                        break;
                }
                row.add(slot);
            }
            board.add(row);
        }
        preComputations();
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
            Util.setCoordinate(board, xCoor, yCoor, Util.wall);
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
            goalNodes.put(Util.getPair(xCoor, yCoor), i);
        }
    }

    public void setSokoban(String line) {
        Scanner word = new Scanner(line);
        int xCoor = (Integer.parseInt(word.next()) - 1);
        int yCoor = (Integer.parseInt(word.next()) - 1);
        root.setPlayerCoordinates(xCoor, yCoor);
        preComputations();
    }

    public void preComputations() {
        System.out.println(root.printBoard(board));
        System.out.flush();
        setDeadPositions();
        setWallPositionsOutside();
        setDeadPositionsAlgo();
        setDistances();
        System.out.flush();
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
                    if (row.get(j) == Util.empty && !goalNodes.containsKey(p)) {
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

    private void findWhiteSpaces() {
        BoardState blankState = Util.getBoard();
        blankState.sokoban.set(root.sokoban);
        ArrayList<BoardState> possibleMoves = Util.getArrayBoardState();
        findPossibleBoxMoves(blankState, possibleMoves, whiteSpaces);
        for (BoardState bs : possibleMoves) {
            Util.recycle(bs);
        }
        Util.recycleABS(possibleMoves);
        Util.recycle(blankState);

        ArrayList<Pair> removePairs = new ArrayList<>();
        for (Pair p : whiteSpaces) {
            byte up = Util.getCoordinate(board, p.getFirst() + Util.getOffsetRow(Util.up), p.getSecond() + Util.getOffsetColumn(Util.up));
            byte down = Util.getCoordinate(board, p.getFirst() + Util.getOffsetRow(Util.down), p.getSecond() + Util.getOffsetColumn(Util.down));
            byte left = Util.getCoordinate(board, p.getFirst() + Util.getOffsetRow(Util.left), p.getSecond() + Util.getOffsetColumn(Util.left));
            byte right = Util.getCoordinate(board, p.getFirst() + Util.getOffsetRow(Util.right), p.getSecond() + Util.getOffsetColumn(Util.right));

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
                    root.sokoban.set(p.getFirst() + Util.getOffsetRow(finalDir), p.getSecond() + Util.getOffsetRow(finalDir));
                    root.movesFromParent.add(finalDir);
                }
                if (!goalNodes.containsKey(p)) {
                    Util.setCoordinate(board, p, Util.wall);
                    removePairs.add(p);
                }
            }
        }
        for (Pair p : removePairs) {
            whiteSpaces.remove(p);
            Util.recycle(p);
        }
    }

    public void setDeadPositionsAlgo() {
        BoardState iterState = Util.getBoard();
        iterState.sokoban.set(root.sokoban);
        for (Pair p : whiteSpaces) {
            if (Util.getCoordinate(board, p) != Util.deadZone && !goalNodes.containsKey(p)) {
                iterState.boxPositions.add(p);
                BoardState solutionBoardState = findSolutionHelper(iterState, Util.dfs, Util.hBoxesOnGoal);
                if (solutionBoardState == null) {
                    Util.setCoordinate(board, p, Util.deadZone);
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

    public void setDistances() {
        HashMap<Pair, Integer> saveGoalNodes = goalNodes;
        BoardState saveRoot = root;
        goalNodes = new HashMap<>();
        root = Util.getBoard();
        root.sokoban.set(saveRoot.sokoban);
        for (Pair playerLocation : whiteSpaces) {
            for (Pair boxLocation : whiteSpaces) {
                if (!playerLocation.equals(boxLocation)) {
                    if (Util.getCoordinate(board, boxLocation) != Util.deadZone) {
                        DoublePair key = new DoublePair(playerLocation, boxLocation);
                        root.addBoxLocation(boxLocation);
                        ArrayList<Integer> goalDistances = new ArrayList<>();
                        for (Map.Entry<Pair, Integer> goal : saveGoalNodes.entrySet()) {
                            goalNodes.put(goal.getKey(), goal.getValue());
                            while (goalDistances.size() < goal.getValue() + 1) {
                                goalDistances.add(Util.maxValueInt);
                            }
                            ArrayList<Byte> solution = Util.getArrayByte();
                            boolean valid = findSolution(solution, Util.huerisitc, Util.hMoveCost, false);
                            goalDistances.set(goal.getValue(), valid ? solution.size() : Util.maxValueInt);
                            Util.recycleAB(solution);
                            goalNodes.clear();
                        }
                        distances.put(key, goalDistances);
                        root.boxPositions.clear();
                    }
                }
            }
        }
        Util.recycle(root);
        goalNodes = saveGoalNodes;
        root = saveRoot;
    }

    /*
     * Search Algorithms
     */

    public void findPossibleBoxMoves(BoardState startState, ArrayList<BoardState> returnMoves, HashSet<Pair> visitableVertices) {
        intpq.add(startState);
        intSeenStates.add(startState);
        if (visitableVertices != null) visitableVertices.add(startState.sokoban.clonePair());

        BoardState state;
        while (!intpq.isEmpty()) {
            state = intpq.removeFirst();
            BoardState child = state.getChild();
            for (byte i = Util.up; i <= Util.down; i++) {
                byte moveType = child.move(board, i);
                if (moveType == Util.invalidMove) {
                    continue;
                }
                if (!intSeenStates.contains(child)) {
                    switch (moveType) {
                        case Util.playerMove:
                            intpq.add(child);
                            intSeenStates.add(child);
                            Pair p = child.sokoban.clonePair();
                            if (visitableVertices != null && !visitableVertices.contains(p)) {
                                visitableVertices.add(p);
                            } else {
                                Util.recycle(p);
                            }
                            break;
                        case Util.boxMove:
                            returnMoves.add(child);
                            intSeenStates.add(child);
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
        for (BoardState b : intSeenStates) {
            if (!returnMoves.contains(b) && !b.equals(startState)) {
                Util.recycle(b);
            }
        }
        intSeenStates.clear();
    }

    public BoardState parseMoves(ArrayList<BoardState> possibleMoves, int searchType, int heuristic) {
        if (searchType == Util.random) {
            int totalHueristic = 0;
            ArrayList<PairBoardState> pairBoardStates = new ArrayList<>();
            for (BoardState boardState : possibleMoves) {
                PairBoardState boardPair = Util.getPairBoard(calculateHueristic(boardState, heuristic), boardState);
                totalHueristic += boardPair.getKey();
                pairBoardStates.add(boardPair);
            }
            int solution = rnd.nextInt(totalHueristic + 1);
            for (PairBoardState pairBoardState : pairBoardStates) {
                solution -= pairBoardState.getKey();
                if (solution <= 0) {
                    pairBoardStates.remove(pairBoardState);
                    for (PairBoardState removePBS : pairBoardStates) {
                        Util.recycle(removePBS.getBoardState());
                        Util.recycle(removePBS);
                    }
                    BoardState returnBoardState = pairBoardState.getBoardState();
                    Util.recycle(pairBoardState);
                    return returnBoardState;
                }
            }
            System.out.println("Impossible Random");
        }
        for (BoardState move : possibleMoves) {
            if (!seenStates.contains(move)) {
                if (searchType == Util.huerisitc) {
                    PairBoardState boardPair = Util.getPairBoard(calculateHueristic(move, heuristic), move);
                    pqH.add(boardPair);
                } else {
                    pq.add(move);
                }
                seenStates.add(move);
            } else {
                Util.recycle(move);
            }
        }
        return null;
    }

    public BoardState nextBoardState(BoardState state, Pair depth, int searchType) {
        if (searchType == Util.huerisitc && pqH.isEmpty()) return null;
        if (searchType != Util.huerisitc && pq.isEmpty()) return null;
        switch (searchType) {
            case Util.bfs:
                return pq.removeFirst();
            case Util.dfs:
                return pq.removeLast();
            case Util.ids:
                if (depth.getFirst() == depth.getSecond()) {
                    BoardState returnState = pq.removeFirst();
                    depth.setFirst(findDepth(returnState));
                    if (depth.getFirst() == depth.getSecond()) {
                        depth.setSecond(depth.getSecond() * depth.getSecond());
                    }
                    return returnState;
                } else {
                    depth.setFirst(depth.getFirst() + 1);
                    return pq.removeLast();

                }
            case Util.huerisitc:
                return pqH.remove().getBoardState();
            case Util.random:
                return state;
            default:
                System.out.println("Invalid searchType");
        }
        return null;
    }

    public BoardState initSearch(BoardState startingState, int searchType) {
        seenStates.add(startingState);
        switch (searchType) {
            case Util.bfs:
            case Util.dfs:
            case Util.ids:
                pq.add(startingState);
                break;
            case Util.huerisitc:
                pqH.add(Util.getPairBoard(0, startingState));
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
            if (state == null) {
                return null;
            }
            ArrayList<BoardState> possibleMoves = Util.getArrayBoardState();
            findPossibleBoxMoves(state, possibleMoves, null);
            for (int i = 0; i < possibleMoves.size(); i++) {
                BoardState move = possibleMoves.get(i);
                if (isGoalState(move)) {
                    for (int j = i + 1; j < possibleMoves.size(); j++) {
                        Util.recycle(possibleMoves.get(j));
                    }
                    Util.recycleABS(possibleMoves);
                    return move;
                }
            }
            state = parseMoves(possibleMoves, searchType, heuristic);
            Util.recycleABS(possibleMoves);
        }
    }

    public boolean findSolution(ArrayList<Byte> returnMoves, int searchType, int heuristic, boolean fullCleanUp) {
        BoardState goalState = findSolutionHelper(root, searchType, heuristic);
        while (searchType == Util.random && goalState == null) {
            goalState = findSolutionHelper(root, searchType, heuristic);
        }
        BoardState iterState = goalState;
        while (iterState != null) {
            returnMoves.addAll(iterState.movesFromParent);
            iterState = iterState.parent;
        }
        if (fullCleanUp) {
            cleanUpAll();
        } else {
            cleanUpReset();
        }
        if (goalState == null) {
            return false;
        }
        Util.recycle(goalState);
        return true;
    }

    private boolean isGoalState(BoardState boardState) {
        for (Pair p : boardState.boxPositions) {
            if (!goalNodes.containsKey(p)) {
                return false;
            }
        }
        return true;
    }

    /*
     * Hueristic Calculations
     */

    public int calculateHueristic(BoardState boardState, int heuristic) {
        switch (heuristic) {
            case Util.hManhattanToAnyGoal:
                return hManhattanToAnyGoal(boardState);
            case Util.hBoxesOnGoal:
                return hblocksOnGoal(boardState);
            case Util.hMoveCost:
                return hMoveCost(boardState);
            default:
                return hManhattanToSingleGoal(boardState);
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

    public int hblocksOnGoal(BoardState state) {
        int counter = 0;
        for (Pair p : state.boxPositions) {
            if (goalNodes.containsKey(p)) {
                counter++;
            }
        }
        return counter;
    }

    public int hManhattanToAnyGoal(BoardState state) {
        int totalDistance = 0;
        int tempDistance = -1;
        for (Pair p : state.boxPositions) {
            for (Pair g : goalNodes.keySet()) {
                tempDistance = Math.max(tempDistance, manhattanDistance(p, g));
            }
            totalDistance += tempDistance;
            tempDistance = -1;
        }
        return totalDistance;
    }

    public int hManhattanToSingleGoal(BoardState state) {
        int totalDistance = 0;
        int tempDistance = -1;
        Pair tempGoalPair = new Pair(-1, -1);
        HashSet<Pair> tempGoalNodes = (HashSet) goalNodes.clone();
        for (Pair p : state.boxPositions) {
            for (Pair g : tempGoalNodes) {
                int dis = manhattanDistance(p, g);
                if (Math.max(tempDistance, dis) == dis) {
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

    private int manhattanDistance(Pair source, Pair destination) {
        return Math.abs(source.first - destination.first) + Math.abs(source.second - destination.second);
    }

    private int euclideanDistanceSquared(Pair source, Pair destination) {
        int temp1 = (destination.first - source.first) * (destination.first - source.first);
        int temp2 = (destination.second - source.second) * (destination.second - source.second);
        return temp1 + temp2;
    }

    private int realDistance(Pair player, Pair box, int goal) {
        DoublePair pair = new DoublePair(player, box);

        return distances.get(pair).get(goal);
    }

    private int hMoveCost(BoardState state) {
        int cost = 0;
        while (state != null) {
            cost += state.movesFromParent.size();
            state = state.parent;
        }
        return cost;
    }

    public int hActualDistanceCost(BoardState state) {
        //ArrayList<Byte> = findSolution(Util.huerisitc, Util.);
        return 0;
    }

    public int minMatching(BoardState state) {
        int i = 0;
        int j = 0;

        int[][] cost = new int[state.boxPositions.size()][goalNodes.size()];
        ArrayList<TreeSet<Pair>> priority = new ArrayList<>();
        HashMap<Integer, Integer> goalToBox = new HashMap<Integer, Integer>();
        for (Pair p : state.boxPositions) {
            TreeSet<Pair> boxCosts = new TreeSet<>();
            System.out.println("The box is " + i);

            for (Pair g : goalNodes.keySet()) {
//        Calculate real cost
                boxCosts.add(new Pair(manhattanDistance(p, g), j));
                System.out.println("The goal is " + j + " " + manhattanDistance(p, g) + " distance");
                j++;
            }
            priority.add(boxCosts);
            j = 0;
            i++;
        }

//    Take the array and find the min matching cost
        for (int k = 0; k < priority.size(); k++) {
            System.out.println(k + "k is ");
            resolveConflicts(priority, goalToBox, k);
        }

        for (Integer in : goalToBox.keySet()) {
            System.out.println(" Goal is " + in + " Box is " + goalToBox.get(in));
        }
        int total = 0;
        for (TreeSet<Pair> treeSet : priority) {
            total += treeSet.first().first;
        }
        System.out.println(total);
        return total;
    }

    private void resolveConflicts(ArrayList<TreeSet<Pair>> priority, HashMap<Integer, Integer> goalToBox, int boxNum) {
        TreeSet<Pair> currentBoxSet = priority.get(boxNum);
        Pair currentMinPair = currentBoxSet.first();
        if (goalToBox.containsKey(currentMinPair.second)) {
            int conflictBoxNum = goalToBox.get(currentMinPair.second);
            TreeSet<Pair> conflictBoxSet = priority.get(conflictBoxNum);
            Pair conflictMinPair = conflictBoxSet.first();

            Iterator<Pair> currentIter = currentBoxSet.iterator();
            Iterator<Pair> conflictIter = conflictBoxSet.iterator();
            Pair currentNextPair = null;
            Pair conflictNext = null;
            for (int i = 0; i < 2; i++) {

                if (currentIter.hasNext()) {
                    currentNextPair = currentIter.next();
                } else {
                    currentNextPair = null;
                }
                if (conflictIter.hasNext()) {
                    conflictNext = conflictIter.next();
                } else {
                    conflictNext = null;

                }
            }
            if (currentNextPair == null) {
                goalToBox.replace(currentMinPair.second, boxNum);
                conflictBoxSet.pollFirst();
                resolveConflicts(priority, goalToBox, conflictBoxNum);
                return;
            } else if (conflictNext == null) {
                currentBoxSet.pollFirst();
                resolveConflicts(priority, goalToBox, boxNum);
                return;

            }
            int currentDifferenceInValue = currentNextPair.first - currentMinPair.first;
            int conflictDifferenceInValue = conflictNext.first - conflictMinPair.first;

            if (currentDifferenceInValue > conflictDifferenceInValue) {
                currentBoxSet.pollFirst();
                resolveConflicts(priority, goalToBox, boxNum);
                return;
            } else {
                goalToBox.replace(currentMinPair.second, boxNum);
//                System.out.println(currentMinPair.second + " is the goal and box num is  " +  conflictBoxNum);

                conflictBoxSet.pollFirst();
                resolveConflicts(priority, goalToBox, conflictBoxNum);
                return;
            }

        }
        goalToBox.put(currentMinPair.second, boxNum);
//        System.out.println(currentMinPair.second + " is the goal and box num is  " +  boxNum);
    }

    /*
     * Clean Up
     */

    public void cleanUpReset() {
        seenStates.remove(root);
        for (BoardState b : pq) {
            seenStates.remove(b);
            Util.recycle(b);
        }
        for (PairBoardState p : pqH) {
            Util.recycle(p);
        }
        for (BoardState b : seenStates) {
            Util.recycle(b);
        }
        pq.clear();
        seenStates.clear();
        pqH.clear();
    }

    public void cleanUpAll() {
        cleanUpReset();
        for (Pair p : whiteSpaces) {
            Util.recycle(p);
        }
        for (Pair p : goalNodes.keySet()) {
            Util.recycle(p);
        }
        for (ArrayList<Byte> ab : board) {
            Util.recycleAB(ab);
        }
        whiteSpaces.clear();
        goalNodes.clear();
        board.clear();
    }


//    public static void main(String args[]) {
//        GameEngine engine = new GameEngine();
//        BoardState state = new BoardState();
//        for (int i = 0; i < 3; i++) {
//
//            state.addBoxLocation(new Pair(i, i * 2));
//            GameEngine.goalNodes.put(new Pair(i + 10, i + 20), i);
//        }
//        engine.minMatching(state);
//    }
}

