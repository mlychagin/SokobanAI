import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;

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
            ArrayList<Byte> row = new ArrayList<Byte>();
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
        setDeadPositions();
    }
    
    public void initInputBoard() throws FileNotFoundException {
        root = Util.getBoard();
        int i = 0;
        //File input = new File("sokoban1.txt");
        
        //Scanner inFile = new Scanner(new FileReader(input));
        Scanner inFile = new Scanner(new File("src/sokoban1.txt"));
        
        while(inFile.hasNextLine()){
            Scanner word = new Scanner(inFile.nextLine());
            ArrayList<Byte> row = new ArrayList<Byte>();
        	if(i == 0){
        		int xSize = Integer.parseInt(word.next());
        		int ySize = Integer.parseInt(word.next());
        		//BoardState.boardDim.setBoardDimensions(xSize, ySize);
        		for(int k = 0; k < ySize; k++){
        			for(int l = 0; l < xSize; l++){
        				row.add(Util.empty);
        			}
        			board.add(row);
        		}
        	}else if(i == 1){
        		word.hasNext(); //int numberOfWallSquares = Integer.parseInt(word.next());
        		while(word.hasNext()){
        			int xCoor = Integer.parseInt(word.next());
            		int yCoor = Integer.parseInt(word.next());
            		
        			board.get(xCoor).set(yCoor,Util.wall);
        		}
        	}else if(i == 2){
        		//int numberOfBoxes = Integer.parseInt(word.next());
        		word.hasNext();
        		while(word.hasNext()){
        			int xCoor = Integer.parseInt(word.next());
            		int yCoor = Integer.parseInt(word.next());
            		
            		root.addBoxLocation(xCoor, yCoor);
        			board.get(xCoor).set(yCoor,Util.box);
        		}
        	}else if(i == 3){
        		//int numberOfGoals = Integer.parseInt(word.next());
        		word.next();
        		while(word.hasNext()){
        			int xCoor = Integer.parseInt(word.next());
            		int yCoor = Integer.parseInt(word.next());
            		
                    goalNodes.add(Util.getPair(xCoor, yCoor));
            		//check for box
            		if(board.get(xCoor).get(yCoor) == Util.box){
            			board.get(xCoor).set(yCoor,Util.boxOnGoal);
            		}else{
            			board.get(xCoor).set(yCoor,Util.goal);
            		}
        		}
        	}else if(i == 4){
        		int xCoor = Integer.parseInt(word.next());     	
        		int yCoor = Integer.parseInt(word.next());
        		
                root.setPlayerCoordinates(xCoor, yCoor);
        		if(board.get(xCoor).get(yCoor) == Util.goal || 
        				board.get(xCoor).get(yCoor) == Util.boxOnGoal){
        			board.get(xCoor).set(yCoor,Util.playerOnGoal);
        		}else{
        			board.get(xCoor).set(yCoor,Util.player);
        		}        	
        	}
        	i++;
        	word.close();
        }
        setDeadPositions();
        inFile.close();
    }

    public void setDeadPositions() {
        boolean keepGoing = true;
        while (keepGoing) {
            keepGoing = false;
            for (int i = 1; i < board.size() - 1; i++) {
                ArrayList<Byte> rowAbove = board.get(i - 1);
                ArrayList<Byte> row = board.get(i);
                ArrayList<Byte> rowBelow = board.get(i + 1);
                for (int j = 1; j < row.size() - 1; j++) {
                    if (row.get(j) == Util.empty) {
                        if (isDeadLock(row, rowAbove, rowBelow, i, j)) {
                            row.set(j, Util.deadZone);
                            keepGoing = true;
                        }
                    }
                }
            }
        }
    }

    private boolean isDeadLock(ArrayList<Byte> row, ArrayList<Byte> above, ArrayList<Byte> below, int rowIndex, int columnIndex) {
        if (goalNodes.contains(Util.getPair(rowIndex, columnIndex))) {
            return false;
        }
        int totalMoves = 0;
        if (moveAble(above.get(columnIndex), below.get(columnIndex))) totalMoves++;
        if (moveAble(below.get(columnIndex), above.get(columnIndex))) totalMoves++;
        if (moveAble(row.get(columnIndex + 1), row.get(columnIndex - 1))) totalMoves++;
        if (moveAble(row.get(columnIndex - 1), row.get(columnIndex + 1))) totalMoves++;
        return totalMoves < 1;
    }

    private boolean moveAble(Byte sokoban, Byte destinationOfBlock) {
        if (sokoban == Util.wall) {
            return false;
        }
        if (destinationOfBlock == Util.deadZone || destinationOfBlock == Util.wall) {
            return false;
        }
        return true;
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

    public BoardState findSolutionBFSHelper() {
        System.out.print(root.printBoard(board));
        priorityQue.add(root);
        seenStates.add(root);

        BoardState state;
        while (true) {
            state = priorityQue.removeFirst();
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

    public BoardState findSolutionDFSHelper() {
        System.out.print(root.printBoard(board));
        priorityQue.add(root);
        seenStates.add(root);

        BoardState state;
        while (true) {
            state = priorityQue.removeLast();
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
        BoardState goalState = findSolutionBFSHelper();

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
