import java.util.ArrayList;
import java.util.HashSet;

public class Zone {
    //TODO HashMap instead of arraylist?
    HashSet<Pair> whiteSpaces = new HashSet<>();
    ArrayList<Pair> moveableBoxes = new ArrayList<>();
    ArrayList<Pair> unMoveableBoxes = new ArrayList<>();
    Zone parent = null;

    public Zone(){

    }

    public boolean containsBox(Pair box){
        if(moveableBoxes.contains(box)) return true;
        return unMoveableBoxes.contains(box);
    }

    boolean isAdjacentHelper(Pair x, Pair y){
        if(x.first == y.first){
            if(x.second + 1 == y.second){
                return true;
            }
            if(x.second - 1 == y.second){
                return true;
            }
        }
        if(x.second == y.second){
            if(x.first + 1 == y.first){
                return true;
            }
            if(x.first - 1 == y.first){
                return true;
            }
        }
        return false;
    }

    public boolean relaxBoxes(Pair x){
        boolean retAdj =false;
        for(int i = unMoveableBoxes.size() - 1; i >= 0; i--){
            Pair y = unMoveableBoxes.get(i);
            if(isAdjacentHelper(x,y)){
                unMoveableBoxes.remove(i);
                moveableBoxes.add(y);
                retAdj = true;
            }
        }
        return retAdj;
    }

    public boolean isAdjacent(Pair x){
        for(Pair y : moveableBoxes){
            if(isAdjacentHelper(x,y)){
                return true;
            }
        }
        return false;
    }

    public boolean allBoxesOnGoal(){
        for(Pair p : moveableBoxes){
            if(!GameEngine.goalNodes.containsKey(p)){
                return false;
            }
        }
        for(Pair p : unMoveableBoxes){
            if(!GameEngine.goalNodes.containsKey(p)){
                return false;
            }
        }
        return true;
    }

    public void reset(){
        for(Pair p : whiteSpaces){
            Util.recycle(p);
        }
        for(Pair p : moveableBoxes){
            Util.recycle(p);
        }
        for(Pair p : unMoveableBoxes){
            Util.recycle(p);
        }
        parent = null;
        whiteSpaces.clear();
        moveableBoxes.clear();
        unMoveableBoxes.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Zone zone = (Zone) o;
        if(whiteSpaces.size() != zone.whiteSpaces.size()) return false;
        if(moveableBoxes.size() != zone.unMoveableBoxes.size()) return false;
        if(unMoveableBoxes.size() != zone.unMoveableBoxes.size()) return false;

        for(Pair p : whiteSpaces){
            if(!zone.whiteSpaces.contains(p)){
                return false;
            }
        }

        for(Pair p : moveableBoxes){
            if(!zone.moveableBoxes.contains(p)){
                return false;
            }
        }

        for(Pair p : unMoveableBoxes){
            if(!zone.unMoveableBoxes.contains(p)){
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        for(Pair p : whiteSpaces){
            result = 31 * p.hashCode();
        }
        for(Pair p : moveableBoxes){
            result = 31 * p.hashCode();
        }
        for(Pair p : unMoveableBoxes){
            result = 31 * p.hashCode();
        }
        return result;
    }
}
