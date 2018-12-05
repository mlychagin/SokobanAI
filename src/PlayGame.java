import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class PlayGame {
    public static void printSolution(ArrayList<Byte> solution){
        Collections.reverse(solution);
        boolean first = true;
        System.out.print("[");
        for(Byte b : solution){
            if(!first){
                System.out.print(",");
            }
            switch (b) {
                case Util.up :
                    System.out.print("Up");
                    break;
                case Util.left :
                    System.out.print("Left");
                    break; 
                case Util.right :
                    System.out.print("Right");
                    break;
                case Util.down :
                    System.out.print("Down");
            }
            first = false;
        }
        System.out.println("]");
        Collections.reverse(solution);
    }

    public static void main (String args[]) throws FileNotFoundException {
    	
        Util util = new Util();
        ArrayList<String> inputMap = new ArrayList<String>();
        
        GameEngine engine = new GameEngine();
        engine.initInputBoard();
        ArrayList<Byte> solution = engine.findSolution(Util.bfs);
        printSolution(solution);
        System.out.println();
        inputMap.clear();
        
    }
}
