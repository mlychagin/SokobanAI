import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
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
    private HashSet<Pair> whiteSpaces = new HashSet<>();
    private BoardState root;

    public GameEngine() {
    }

    public void setDeadlocks(){
        System.out.println(root.printBoard(board));
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

    public void setBoardSize(String line){
        Scanner word = new Scanner(line);
        int xSize = Integer.parseInt(word.next());
        int ySize = Integer.parseInt(word.next());
        for(int k = 0; k < ySize; k++){
            ArrayList<Byte> row = new ArrayList<>();
            for(int l = 0; l < xSize; l++){
                row.add(Util.empty);
            }
            board.add(row);
        }
    }

    public void setWalls(String line){
        Scanner word = new Scanner(line);
        int nWalls = Integer.parseInt(word.next());
        for(int i = 0; i < nWalls; i++){
            int xCoor = (Integer.parseInt(word.next())-1);
            int yCoor = (Integer.parseInt(word.next())-1);
            BoardState.setCoordinate(board, xCoor, yCoor, Util.wall);
        }
    }

    public void setBoxes(String line){
        Scanner word = new Scanner(line);
        int nBoxes = Integer.parseInt(word.next());
        for(int i = 0; i < nBoxes; i++){
            int xCoor = (Integer.parseInt(word.next())-1);
            int yCoor = (Integer.parseInt(word.next())-1);
            root.addBoxLocation(xCoor, yCoor);
        }
    }

    public void setSokoban(String line){
        Scanner word = new Scanner(line);
        int xCoor = (Integer.parseInt(word.next())-1);
        int yCoor = (Integer.parseInt(word.next())-1);
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
        if(visitableVertices != null) visitableVertices.add(startState.sokoban.clonePair());

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

    public BoardState findSolutionHelper(BoardState startingState, int searchType) {
        priorityQue.add(startingState);
        seenStates.add(startingState);
        int depth = 0;
        int depthRequirement = findInitDepthRequirement();

        BoardState state = null;
        while (true) {
            if (priorityQue.size() == 0) return null;
            switch (searchType) {
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
                    break;
                default:
                    System.out.println("Invalid searchType");
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
            ArrayList<BoardState> possibleMoves = Util.getArrayBoardState();
            findPossibleBoxMoves(state, possibleMoves, null);
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
        for(Pair p : whiteSpaces){
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

    public ArrayList<Byte> findSolution(int searchType) {
        ArrayList<Byte> returnMoves = Util.getArrayByte();
        BoardState goalState = findSolutionHelper(root, searchType);

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

        ArrayList<Pair> removePairs = new ArrayList<>();
        for(Pair p : whiteSpaces){
            byte up = BoardState.getCoordinate(board, p.getFirst() + BoardState.getOffsetRow(Util.up), p.getSecond() + BoardState.getOffsetColumn(Util.up));
            byte down = BoardState.getCoordinate(board, p.getFirst() + BoardState.getOffsetRow(Util.down), p.getSecond() + BoardState.getOffsetColumn(Util.down));
            byte left = BoardState.getCoordinate(board, p.getFirst() + BoardState.getOffsetRow(Util.left), p.getSecond() + BoardState.getOffsetColumn(Util.left));
            byte right = BoardState.getCoordinate(board, p.getFirst() + BoardState.getOffsetRow(Util.right), p.getSecond() + BoardState.getOffsetColumn(Util.right));

            int count = 0;
            byte finalDir = 0;
            if(up == Util.wall){
                count++;
            } else {
                finalDir = Util.up;
            }
            if(down == Util.wall){
                count++;
            } else {
                finalDir =  Util.down;
            }
            if(left == Util.wall){
                count++;
            } else {
                finalDir = Util.left;
            }
            if(right == Util.wall){
                count++;
            } else {
                finalDir = Util.right;
            }
            if(count == 3){
                if(root.sokoban.equals(p)){
                    root.sokoban.set(p.getFirst() + BoardState.getOffsetRow(finalDir), p.getSecond() + BoardState.getOffsetRow(finalDir));
                    root.movesFromParent.add(finalDir);
                }
                if(!goalNodes.contains(p)){
                    BoardState.setCoordinate(board, p, Util.wall);
                    removePairs.add(p);
                }
            }
        }
        for(Pair p : removePairs){
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
            if(BoardState.getCoordinate(board, p) != Util.deadZone && !goalNodes.contains(p)){
                iterState.boxPositions.add(p);
                BoardState solutionBoardState = findSolutionHelper(iterState, Util.dfs);
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
    }
}
