import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
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
                System.out.println(line);
                GameEngine engine = new GameEngine();
                engine.initBoard(inputMap);
                ArrayList<Byte> solution = engine.findSolution();
                //System.out.println(Arrays.toString(solution.toArray()));
                System.out.println();
                inputMap.clear();
            } else {
                inputMap.add(line);
            }
        }
    }
}
