import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class GameEngine{
    private LinkedList<BoardState> priorityQue = new LinkedList<>();
    private HashSet<BoardState> seenStates = new HashSet<>();
    ArrayList<ArrayList<Byte>> board = new ArrayList<>();
    static HashSet<Pair> goalNodes = new HashSet<>();
    BoardState root = Util.getBoard();

    public GameEngine(){
    }

    public void initBoard(ArrayList<String> map){
        root = Util.getBoard();
        for(int i = 0; i < map.size(); i++){
            String s = map.get(i);
            ArrayList<Byte> row = new ArrayList<Byte>();
            for(int j = 0; j < s.length(); j++){
                byte slot = (byte)s.charAt(j);
                switch (slot){
                    case Util.box:
                        root.addBoxLocation(i,j);
                        slot = Util.empty;
                        break;
                    case Util.goal:
                        goalNodes.add(Util.getPair(i, j));
                        break;
                    case Util.boxOnGoal:
                        root.addBoxLocation(i,j);
                        goalNodes.add(Util.getPair(i, j));
                        slot = Util.goal;
                        break;
                    case Util.player:
                        root.setPlayerCoordinates(i,j);
                        slot = Util.empty;
                        break;
                    case Util.playerOnGoal:
                        goalNodes.add(Util.getPair(i, j));
                        root.setPlayerCoordinates(i,j);
                        slot = Util.goal;
                        break;
                    default:
                        break;
                }
                row.add(slot);
            }
            board.add(row);
        }
    }

    public ArrayList<Byte> findSolution(){
        System.out.println(root.printBoard(board));
        ArrayList<Byte> returnMoves = new ArrayList<>();
        priorityQue.add(root);
        seenStates.add(root);

        BoardState state = root;
        while(!priorityQue.isEmpty()){
            state = priorityQue.removeFirst();
            if(isGoalState(state)){
                break;
            }
            BoardState child = state.getChild();
            for(byte i = Util.up; i <= Util.down; i++){
                if(child.move(board, i)){
                    if(!seenStates.contains(child)){
                        child.moveFromParent = i;
                        priorityQue.add(child);
                        seenStates.add(child);
                        //System.out.println(child.printBoard(board));
                    } else {
                        Util.recycle(child);
                    }
                    child = state.getChild();
                }
            }
            Util.recycle(child);
        }
        //System.out.println(state.printBoard(board));
        while(state.parent != null){
            returnMoves.add(state.moveFromParent);
            state = state.parent;
        }
        for(BoardState b : seenStates){
            Util.recycle(b);
        }
        seenStates.clear();
        for(Pair p : goalNodes){
            Util.recycle(p);
        }
        goalNodes.clear();
        board.clear();
        return returnMoves;
    }

    public boolean isGoalState(BoardState boardState){
        for(Pair p : boardState.boxPositions){
            if(!goalNodes.contains(p)){
                return false;
            }
        }
        System.out.println("Finished");
        return true;
    }
}
