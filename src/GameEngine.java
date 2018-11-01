import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class GameEngine{
    BoardState root;
    ArrayList<Integer> goalNodes = new ArrayList();

    public GameEngine(){
    }

    public void initBoard(ArrayList<String> map){
        root = Util.getBoard();
        for(int i = 0; i < map.size(); i++){
            String s = map.get(i);
            ArrayList<Byte> row = new ArrayList<Byte>();
            for(int j = 0; j < s.length(); j++){
                byte slot = (byte)s.charAt(j);
                if(slot == Util.player || slot == Util.playerOnGoal){
                    root.setPlayerCoordinates(i,j);
                }
                if(slot == Util.goal || slot == Util.playerOnGoal || slot == Util.boxOnGoal){
                    goalNodes.add(i);
                    goalNodes.add(j);
                }
                row.add((byte)s.charAt(j));
            }
            root.board.add(row);
        }
    }

    public ArrayList<Byte> findSolution(){
        PriorityQueue<BoardState> queue = new PriorityQueue<>(11, new BoardState.BoardStateCompare());
        queue.addAll(generatePossibleMoves(root));
        while(queue.size() > 0){
            BoardState state = queue.poll();
            if(isFinished(state)){
                return state.movesFromParent;
            }
            queue.addAll(generatePossibleMoves(state));
        }
        return null;
    }

    private boolean isFinished(BoardState state){
        for(int i = 0; i < goalNodes.size(); i+=2){
            if(state.getCoordinate(goalNodes.get(i), goalNodes.get(i+1)) != Util.boxOnGoal){
                return false;
            }
        }
        return true;
    }

    public ArrayList<BoardState> generatePossibleMoves(BoardState root){
        ArrayList<BoardState> returnMoves = new ArrayList<>();
        HashSet<BoardState> seenStates = new HashSet<>();
        LinkedList<BoardState> priorityQue = new LinkedList<>();
        seenStates.add(root);
        priorityQue.add(root);

        while(!priorityQue.isEmpty()){
            BoardState state = priorityQue.removeFirst();
            BoardState copyState = state.clone();
            for(byte i = Util.up; i <= Util.down; i++){
                byte result = copyState.move(i);
                if(result == Util.playerMove){
                    if(!seenStates.contains(copyState)){
                        copyState.movesFromParent.add(i);
                        seenStates.add(copyState);
                        priorityQue.add(copyState);
                    }
                } else if(result == Util.boxMove){
                    BoardState iterator = copyState.parent;
                    copyState.movesFromParent.add(i);
                    while(iterator != null){
                        copyState.movesFromParent.addAll(iterator.movesFromParent);
                        iterator = iterator.parent;
                    }
                    copyState.parent = null;
                    copyState.totalMoves = copyState.movesFromParent.size();
                    returnMoves.add(copyState);
                } else {
                    //TODO : Possibly Revert the move instead of copying all over again.
                    Util.recycle(copyState);
                }
                copyState = state.clone();
            }
        }
        return returnMoves;
    }
}
