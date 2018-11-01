import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class PlayGame {
    public static void main (String args[]) throws FileNotFoundException {
        ArrayList<String> inputMap = new ArrayList<String>();
        Scanner inFile = new Scanner(new FileReader(args[0]));
        while (inFile.hasNext()){
            String line = inFile.nextLine();
            if(line.length() == 0){
                continue;
            }
            if(line.contains(";")){
                GameEngine engine = new GameEngine();
                engine.initBoard(inputMap);
                BoardState copyBoard = engine.root.clone();
                System.out.println(engine.root.toString());
                ArrayList<Byte> solution = engine.findSolution();
                for(int i = solution.size()-1; i >=0; i--){
                    copyBoard.move(solution.get(i));
                }
                System.out.println(copyBoard);
                System.out.println(Arrays.toString(solution.toArray()));
                inputMap.clear();
            } else {
                inputMap.add(line);
            }
        }
    }
}
