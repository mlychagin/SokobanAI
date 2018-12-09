import java.util.*;

public class GameEngine {
    private HashMap<BoardState, BoardState> seenStates = new HashMap<>();
    private PriorityQueue<BoardState> pqH = new PriorityQueue<>();
    private LinkedList<BoardState> pq = new LinkedList<>();
    private LinkedList<BoardState> intpq = new LinkedList<>();
    private LinkedList<Zone> pqZ = new LinkedList<>();
    private HashSet<Zone> seenZones = new HashSet<>();
    private HashSet<BoardState> intSeenStates = new HashSet<>();
    private HashMap<DoublePair, ArrayList<Integer>> distances = new HashMap<>();
    ArrayList<ArrayList<Byte>> board = new ArrayList<>();
    static HashMap<Pair, Integer> goalNodes = new HashMap<>();
    private HashSet<Pair> whiteSpaces = new HashSet<>();

    private ArrayList<PriorityQueue<Pair>> priority = new ArrayList<>();
    private HashMap<Integer, Integer> goalToBox = new HashMap<Integer, Integer>();
    BoardState root;
    private boolean startPruning = false;
    private Random rnd = new Random();

    static boolean tunneling = true;
    static boolean zoneDetection = false;
    static boolean hminPruning = false;
    static boolean deadLockDetection = true;

    LinkedList<BoardState> boardPool = Util.boardPool;
    LinkedList<Pair> pairPool = Util.pairPool;

    public GameEngine() {
    }

    /*
     * Initialization
     */

    public void initHelper(ArrayList<String> map) {
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
    }

    public void initFull(ArrayList<String> map) {
        initHelper(map);
        ;
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
        if (deadLockDetection) {
            setDeadPositions();
            setWallPositionsOutside();
            setDeadPositionsAlgo();
            setDistances();
            startPruning = true;
        }
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
                            row.set(j, Util.deadSquare);
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
        return destinationOfBlock != Util.deadSquare && destinationOfBlock != Util.wall && sokoban != Util.wall;
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

    private void findWhiteSpacesHelper() {
        BoardState blankState = Util.getBoard();
        blankState.sokoban.set(root.sokoban);
        findPossibleBoxMoves(blankState, null, whiteSpaces, null, Util.noZoneChecking);
        Util.recycle(blankState);
    }

    private void findWhiteSpaces() {
        findWhiteSpacesHelper();
        for (Pair p : whiteSpaces) {
            if (goalNodes.containsKey(p)) {
                continue;
            }
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
                Pair endLocation = Util.getPair(p.getFirst() + Util.getOffsetRow(finalDir), p.getSecond() + Util.getOffsetColumn(finalDir));
                if (root.sokoban.equals(p)) {
                    if (root.boxPositions.contains(endLocation)) {
                        Pair box = root.boxPositions.get(root.boxPositions.indexOf(endLocation));
                        Pair endBoxLocation = Util.getPair(box.getFirst() + Util.getOffsetRow(finalDir), box.getSecond() + Util.getOffsetColumn(finalDir));
                        if (Util.getCoordinate(board, endBoxLocation) != Util.wall) {
                            root.sokoban.set(p.getFirst() + Util.getOffsetRow(finalDir), p.getSecond() + Util.getOffsetColumn(finalDir));
                            root.movesFromParent.add(finalDir);
                            box.set(endBoxLocation);
                        }
                        Util.recycle(endBoxLocation);
                    } else {
                        root.sokoban.set(p.getFirst() + Util.getOffsetRow(finalDir), p.getSecond() + Util.getOffsetColumn(finalDir));
                        root.movesFromParent.add(finalDir);
                    }
                }
                Util.recycle(endLocation);
                if (!goalNodes.containsKey(p)) {
                    Util.setCoordinate(board, p, Util.wall);
                }
            }
        }
        for (Pair p : whiteSpaces) {
            Util.recycle(p);
        }
        whiteSpaces.clear();
        findWhiteSpacesHelper();
    }

    public void setDeadPositionsAlgo() {
        for (Pair p : whiteSpaces) {
            if (Util.getCoordinate(board, p) != Util.deadSquare && !goalNodes.containsKey(p) && !p.equals(root.sokoban)) {
                BoardState iterState = Util.getBoard();
                iterState.sokoban.set(root.sokoban);
                iterState.boxPositions.add(p);
                BoardState solutionBoardState = findSolutionHelper(iterState, Util.dfs, Util.hBoxesOnGoal, Util.hRealCost);
                if (solutionBoardState == null) {
                    Util.setCoordinate(board, p, Util.deadSquare);
                }
                iterState.boxPositions.clear();
                cleanUpReset();
            }
        }
    }

    public void setDistances() {
        HashMap<Pair, Integer> saveGoalNodes = goalNodes;
        BoardState saveRoot = root;
        goalNodes = new HashMap<>();
        root = Util.getBoard();
        for (Pair playerLocation : whiteSpaces) {
            root.sokoban.set(playerLocation);
            for (Pair boxLocation : whiteSpaces) {
                if (!playerLocation.equals(boxLocation)) {
                    if (Util.getCoordinate(board, boxLocation) != Util.deadSquare) {
                        DoublePair key = Util.getDoublePair(playerLocation, boxLocation);
                        root.addBoxLocation(boxLocation);
                        //TODO Recycle ArrayList Integer
                        ArrayList<Integer> goalDistances = new ArrayList<>();
                        for (Map.Entry<Pair, Integer> goal : saveGoalNodes.entrySet()) {
                            goalNodes.put(goal.getKey(), goal.getValue());
                            while (goalDistances.size() < goal.getValue() + 1) {
                                goalDistances.add(Util.maxValueInt);
                            }
                            ArrayList<Byte> solution = Util.getArrayByte();
                            boolean valid = findSolution(solution, Util.huerisitc, Util.hMoveCost, Util.hManhattan, false);
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

    public void findPossibleBoxMoves(BoardState startState, ArrayList<BoardState> returnMoves, HashSet<Pair> visitableVertices, ArrayList<Zone> zones, byte type) {
        Zone zone = null;
        boolean toZone = type == Util.zoneChecking || type == Util.zoneCheckingHelper;
        if (toZone) {
            zone = Util.getZone();
            zones.add(zone);
            visitableVertices = zone.whiteSpaces;
        }
        intpq.add(startState);
        intSeenStates.add(startState);
        if (visitableVertices != null) visitableVertices.add(startState.sokoban.clonePair());

        BoardState state;
        while (!intpq.isEmpty()) {
            state = intpq.removeFirst();
            BoardState child = state.getChild();
            for (byte i = Util.up; i <= Util.down; i++) {
                PairPairByte ret = child.move(board, i);
                if (ret.returnType == Util.invalidMove || ret.returnType == Util.invalidBoxMove) {
                    if (ret.returnType == Util.invalidBoxMove && toZone) {
                        zone.unMoveableBoxes.add(Util.getPair(ret.boxLocation.getFirst(), ret.boxLocation.getSecond()));
                    }
                    Util.recycle(ret);
                    continue;
                }
                if (!intSeenStates.contains(child)) {
                    switch (ret.returnType) {
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
                            if (hminPruning && startPruning && calculateHueristic(child, Util.hMinMatching, Util.hRealCost) == Util.maxValueInt) {
                                break;
                            }
                            if (toZone) {
                                zone.moveableBoxes.add(Util.getPair(ret.boxLocation.getFirst(), ret.boxLocation.getSecond()));
                            }
                            intSeenStates.add(child);
                            if (returnMoves != null) {
                                returnMoves.add(child);
                                ArrayList<Byte> childMovesFromParent = child.movesFromParent;
                                child = child.parent;
                                assert child != null;
                                assert startState != null;
                                while (!child.equals(startState)) {
                                    childMovesFromParent.addAll(child.movesFromParent);
                                    child = child.parent;
                                }
                                returnMoves.get(returnMoves.size() - 1).parent = startState;
                            } else {
                                Util.recycle(child);
                            }
                            break;
                        default:
                            System.out.println("Incorrect Move");
                            break;

                    }
                } else {
                    Util.recycle(child);
                }
                child = state.getChild();
                Util.recycle(ret);
            }
            Util.recycle(child);
        }
        for (BoardState b : intSeenStates) {
            if (b.equals(startState)) {
                continue;
            }
            if (returnMoves != null && returnMoves.contains(b)) {
                continue;
            }
            Util.recycle(b);
        }
        intSeenStates.clear();


        if (type == Util.zoneChecking) {
            if (!isZoneValid(startState, zones, zone)) {
                assert returnMoves != null;
                for (BoardState bs : returnMoves) {
                    Util.recycle(bs);
                }
                returnMoves.clear();
            }
            for (Zone z : zones) {
                Util.recycle(z);
            }
            zones.clear();
        }
    }

    public boolean relaxEdge(Zone a, Zone b) {
        boolean wasRelaxed = false;
        for (Pair p : a.moveableBoxes) {
            boolean ret = b.relaxBoxes(p);
            if (!wasRelaxed && ret) {
                wasRelaxed = true;
            }
        }
        return wasRelaxed;
    }

    public boolean edgeExists(Zone a, Zone b) {
        for (Pair p : a.moveableBoxes) {
            if (b.containsBox(p) || b.isAdjacent(p)) {
                return true;
            }
        }
        if (b.allBoxesOnGoal()) {
            return true;
        }
        return false;
    }

    public boolean isZoneValid(BoardState startState, ArrayList<Zone> zones, Zone zone) {
        while (true) {
            boolean isFinished = true;
            for (Pair p : whiteSpaces) {
                if (startState.boxPositions.contains(p)) {
                    continue;
                }
                boolean contains = false;
                for (Zone z : zones) {
                    if (z.whiteSpaces.contains(p)) {
                        contains = true;
                    }
                }
                if (!contains) {
                    BoardState boardState = startState.getChild();
                    boardState.sokoban.set(p);
                    findPossibleBoxMoves(boardState, null, null, zones, Util.zoneCheckingHelper);
                    isFinished = false;
                    break;
                }
            }
            if (isFinished) {
                break;
            }
        }

        zone.parent = zone;
        seenZones.add(zone);
        pqZ.add(zone);

        int prevReachable = Util.maxValueInt;

        boolean changes = true;
        while (changes) {
            changes = false;
            for (Zone a : zones) {
                for (Zone b : zones) {
                    if (!a.equals(b)) {
                        if (relaxEdge(a, b)) {
                            changes = true;
                        }
                    }
                }
            }
        }

        while (prevReachable != getReachableZones(zones)) {
            prevReachable = getReachableZones(zones);
            while (!pqZ.isEmpty()) {
                Zone zA = pqZ.poll();
                for (Zone zB : zones) {
                    if (zA.equals(zB)) {
                        continue;
                    }
                    if (edgeExists(zA, zB)) {
                        if (!seenZones.contains(zB)) {
                            seenZones.add(zB);
                            zB.parent = zA;
                            pqZ.add(zB);
                        }
                    }
                }
            }
            seenZones.clear();
            pqZ.clear();
        }

        for (Zone z : zones) {
            if (z.parent == null) {
                return false;
            }
        }
        return true;
    }

    private int getReachableZones(ArrayList<Zone> zones) {
        int total = 0;
        for (Zone z : zones) {
            if (z.parent != null) {
                total++;
            }
        }
        return total;
    }

    public BoardState parseMoves(ArrayList<BoardState> possibleMoves, int searchType, int heuristic, int distanceType) {
        if (searchType == Util.randomH || searchType == Util.random) {
            int totalHueristic = 0;
            for (BoardState boardState : possibleMoves) {
                boardState.hueristicValue = searchType == Util.random ? 1 : calculateHueristic(boardState, heuristic, distanceType);
                totalHueristic += boardState.hueristicValue;
            }
            int solution = rnd.nextInt(totalHueristic + 1);
            for (BoardState boardState : possibleMoves) {
                solution -= boardState.hueristicValue;
                if (solution <= 0) {
                    possibleMoves.remove(boardState);
                    for (BoardState removeBS : possibleMoves) {
                        Util.recycle(removeBS);
                    }
                    return boardState;
                }
            }
            System.out.println("Impossible Random");
        }
        for (BoardState move : possibleMoves) {
            if (!seenStates.containsKey(move)) {
                if (searchType == Util.huerisitc) {
                    move.hueristicValue = calculateHueristic(move, heuristic, distanceType);
                    pqH.add(move);
                } else {
                    pq.add(move);
                }
                seenStates.put(move, move);
            } else {
                BoardState saved = seenStates.get(move);
                int savedCost = calculateHueristic(saved, Util.hMoveCost, Util.hManhattan);
                int newCost = calculateHueristic(move, Util.hMoveCost, Util.hManhattan);
                if (newCost < savedCost) {
                    saved.parent = move.parent;
                    saved.movesFromParent.clear();
                    saved.movesFromParent.addAll(move.movesFromParent);
                }
                Util.recycle(move);
            }
        }
        return null;
    }

    public BoardState nextBoardState(BoardState state, Pair depth, int searchType) {
        if(searchType == Util.random) return state;
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
                return pqH.poll();
            default:
                System.out.println("Invalid searchType");
        }
        return null;
    }

    public BoardState initSearch(BoardState startingState, int searchType) {
        seenStates.put(startingState, startingState);
        switch (searchType) {
            case Util.bfs:
            case Util.dfs:
            case Util.ids:
                pq.add(startingState);
                break;
            case Util.huerisitc:
                pqH.add(startingState);
                break;
            case Util.random:
                seenStates.remove(startingState);
                return startingState;
            default:
                System.out.println("Invalid searchType");
        }
        return null;
    }

    public BoardState findSolutionHelper(BoardState startingState, int searchType, int heuristic, int distanceType) {
        BoardState state = initSearch(startingState, searchType);
        Pair depth = Util.getPair(0, findInitDepthRequirement());
        while (true) {
            state = nextBoardState(state, depth, searchType);
            if (state == null) {
                Util.recycle(depth);
                return null;
            }
            if (isGoalState(state)) {
                Util.recycle(depth);
                return state;
            }
            ArrayList<BoardState> possibleMoves = Util.getArrayBoardState();
            ArrayList<Zone> zones = new ArrayList<>();
            findPossibleBoxMoves(state, possibleMoves, null, zones, zoneDetection && startPruning ? Util.zoneChecking : Util.noZoneChecking);
            zones.clear();
            state = parseMoves(possibleMoves, searchType, heuristic, distanceType);
            Util.recycleABS(possibleMoves);
        }
    }

    public boolean findSolution(ArrayList<Byte> returnMoves, int searchType, int heuristic, int distanceType, boolean fullCleanUp) {
        BoardState goalState = findSolutionHelper(root, searchType, heuristic, distanceType);
        while (searchType == Util.random && goalState == null) {
            goalState = findSolutionHelper(root, searchType, heuristic, distanceType);
        }
        BoardState iterState = goalState;
        while (iterState != null) {
            returnMoves.addAll(iterState.movesFromParent);
            iterState = iterState.parent;
        }
        if (fullCleanUp) {
            cleanUpAll(true);
        } else {
            cleanUpReset();
        }
        return goalState != null;
    }

    public boolean isGoalState(BoardState boardState) {
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

    public int calculateHueristic(BoardState boardState, int heuristic, int distanceType) {
        switch (heuristic) {
            case Util.hToAnyGoal:
                return hToAnyGoal(boardState, distanceType);
            case Util.hSingleGoal:
                return hToSingleGoal(boardState, distanceType);
            case Util.hMoveCost:
                return hMoveCost(boardState);
            case Util.hMinMatching:
                return minMatching(boardState, distanceType);
            default:
                return hblocksOnGoal(boardState);
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

    public int hToAnyGoal(BoardState state, int type) {
        int totalDistance = 0;
        int tempDistance = -2;
        for (Pair p : state.boxPositions) {
            for (Pair g : goalNodes.keySet()) {

                switch (type) {
                    case Util.hManhattan:
                        tempDistance = Math.max(tempDistance, manhattanDistance(p, g));
                        break;
                    case Util.hEuclidean:
                        tempDistance = Math.max(tempDistance, euclideanDistanceSquared(p, g));
                        break;
                    case Util.hRealCost:
                        tempDistance = Math.max(tempDistance, realDistance(state.sokoban, p, goalNodes.get(g)));

                }
            }
            totalDistance += tempDistance;
            tempDistance = -2;
        }
        return totalDistance;
    }

    public int hToSingleGoal(BoardState state, int distanceType) {
        int totalDistance = 0;
        int tempDistance = -1;
        Pair tempGoalPair = Util.getPair(-1, -1);
        HashSet<Pair> tempGoalNodes = (HashSet) goalNodes.clone();
        for (Pair p : state.boxPositions) {
            for (Pair g : tempGoalNodes) {

                int dis;
                if (distanceType == Util.hManhattan) {
                    dis = manhattanDistance(p, g);
                } else {
                    dis = euclideanDistanceSquared(p, g);
                }
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
        DoublePair pair = Util.getDoublePair(player, box);
        int distance = distances.get(pair).get(goal);
        Util.recycle(pair);
        return distance;
    }

    private int hMoveCost(BoardState state) {
        int cost = 0;
        while (state != null) {
            cost += state.movesFromParent.size();
            state = state.parent;
        }
        return cost;
    }

    public int minMatching(BoardState state, int distance) {
        int i = 0;
        int j = 0;

        for (Pair p : state.boxPositions) {
            PriorityQueue<Pair> boxCosts = Util.getPriorityQueue();
            for (Pair g : goalNodes.keySet()) {
                switch (distance) {
                    case Util.hManhattan:
                        boxCosts.add(Util.getPair(manhattanDistance(p, g), j));
                        break;
                    case Util.hEuclidean:
                        boxCosts.add(Util.getPair(euclideanDistanceSquared(p, g), j));
                        break;
                    case Util.hRealCost:
                        Pair pair = Util.getPair(realDistance(state.sokoban, p, goalNodes.get(g)), j);
                        boxCosts.add(pair);
                }
                j++;
            }
            priority.add(boxCosts);
            j = 0;
            i++;
        }
        for (int k = 0; k < priority.size(); k++) {
            resolveConflicts(priority, goalToBox, k);
        }
        int total = 0;
        for (PriorityQueue<Pair> treeSet : priority) {
            int tempTotal = treeSet.peek().first;
            if (tempTotal == Integer.MAX_VALUE) {
                total = Integer.MAX_VALUE;
            } else if (total != Integer.MAX_VALUE) {
                total += tempTotal;
            }
            for (Pair p : treeSet) {
                Util.recycle(p);
            }
            Util.recyclePriority(treeSet);
        }
        priority.clear();
        goalToBox.clear();
        return total;
    }

    private void resolveConflicts(ArrayList<PriorityQueue<Pair>> priority, HashMap<Integer, Integer> goalToBox, int boxNum) {
        PriorityQueue<Pair> currentBoxSet = priority.get(boxNum);
        Pair currentMinPair = currentBoxSet.peek();
        if (goalToBox.containsKey(currentMinPair.second)) {
            int conflictBoxNum = goalToBox.get(currentMinPair.second);
            PriorityQueue<Pair> conflictBoxSet = priority.get(conflictBoxNum);
            Pair conflictMinPair = conflictBoxSet.peek();

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
                Util.recycle(conflictBoxSet.poll());
                resolveConflicts(priority, goalToBox, conflictBoxNum);
                return;
            } else if (conflictNext == null) {
                Util.recycle(currentBoxSet.poll());
                resolveConflicts(priority, goalToBox, boxNum);
                return;

            }
            int currentDifferenceInValue = currentNextPair.first - currentMinPair.first;
            int conflictDifferenceInValue = conflictNext.first - conflictMinPair.first;
            if (currentDifferenceInValue > conflictDifferenceInValue) {
                Util.recycle(currentBoxSet.poll());
                resolveConflicts(priority, goalToBox, boxNum);
                return;
            } else {
                goalToBox.replace(currentMinPair.second, boxNum);
                Util.recycle(conflictBoxSet.poll());
                resolveConflicts(priority, goalToBox, conflictBoxNum);
                return;
            }
        }
        goalToBox.put(currentMinPair.second, boxNum);
    }

    /*
     * Clean Up
     */

    public void cleanUpReset() {
        seenStates.remove(root);
        for (BoardState b : seenStates.keySet()) {
            Util.recycle(b);
        }
        pq.clear();
        seenStates.clear();
        pqH.clear();
    }

    public void cleanUpAll(boolean printStateCount) {
        if (printStateCount) {
            System.out.print(seenStates.size() + ",");
        }
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
        for (DoublePair p : distances.keySet()) {
            Util.recycle(p);
        }
        Util.recycle(root);
        distances.clear();
        whiteSpaces.clear();
        goalNodes.clear();
        board.clear();
        startPruning = false;
    }
}

