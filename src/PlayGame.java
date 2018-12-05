import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.UTFDataFormatException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class PlayGame {
    public static void printSolution(ArrayList<Byte> solution) {
        Collections.reverse(solution);
        boolean first = true;
        System.out.print("[");
        for (Byte b : solution) {
            if (!first) {
                System.out.print(",");
            }
            switch (b) {
                case Util.up:
                    System.out.print("Up");
                    break;
                case Util.left:
                    System.out.print("Left");
                    break;
                case Util.right:
                    System.out.print("Right");
                    break;
                case Util.down:
                    System.out.print("Down");
            }
            first = false;
        }
        System.out.println("]");
        Collections.reverse(solution);
    }

    public static void main(String args[]) throws FileNotFoundException {
        ArrayList<String> inputMap = new ArrayList<>();
        Scanner inFile = new Scanner(new FileReader(args[0]));
        while (inFile.hasNext()) {
            String line = inFile.nextLine();
            if (line.length() == 0) {
                continue;
            }
            if (line.contains(";")) {
                System.out.println(line);
                GameEngine engine = new GameEngine();
                engine.initBoard(inputMap);
                ArrayList<Byte> solution = engine.findSolution(Util.huerisitc);
                printSolution(solution);
                Util.recycleAB(solution);
                int countBP = Util.getCountBoardPool();
                int sizeBP = Util.getSizeBoardPool();

                int countPP = Util.getCountPairPool();
                int sizePP = Util.getSizePairPool();

                int countABSP = Util.getCountArrayBoardStatePool();
                int sizeABSP = Util.getSizeArrayBoardStatePool();

                int countABP = Util.getCountArrayBytePool();
                int sizeABP = Util.getSizeArrayBytePool();

                System.out.flush();
                inputMap.clear();
            } else {
                inputMap.add(line);
            }
        }
    }
}
