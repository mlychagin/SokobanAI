import java.io.FileNotFoundException;
import java.io.FileReader;
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
            System.out.print(Util.byteToString(b));
            first = false;
        }
        System.out.println("]");
        Collections.reverse(solution);
    }

    private static void statLeak() {
        int countBP = Util.getCountBoardPool();
        int sizeBP = Util.getSizeBoardPool();

        int countPP = Util.getCountPairPool();
        int sizePP = Util.getSizePairPool();

        int countABSP = Util.getCountArrayBoardStatePool();
        int sizeABSP = Util.getSizeArrayBoardStatePool();

        int countABP = Util.getCountArrayBytePool();
        int sizeABP = Util.getSizeArrayBytePool();
        System.out.flush();
        System.out.flush();
    }

    private static void parseProf(String args[]) throws FileNotFoundException {
        Scanner inFile = new Scanner(new FileReader(args[0]));
        GameEngine engine = new GameEngine();
        engine.setBoardSize(inFile.nextLine());
        engine.setWalls(inFile.nextLine());
        engine.setBoxes(inFile.nextLine());
        engine.setGoals(inFile.nextLine());
        engine.setSokoban(inFile.nextLine());
        ArrayList<Byte> solution = Util.getArrayByte();
        engine.findSolution(solution, Util.bfs, Util.hBoxesOnGoal, true);
        printSolution(solution);
        Util.recycleAB(solution);
        statLeak();
    }

    public static void parseFile(String args[]) throws FileNotFoundException {
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
                ArrayList<Byte> solution = Util.getArrayByte();
                engine.findSolution(solution, Util.dfs, Util.hBoxesOnGoal, true);
                printSolution(solution);
                Util.recycleAB(solution);
                statLeak();
                inputMap.clear();
            } else {
                inputMap.add(line);
            }
        }
    }

    public static void main(String args[]) throws FileNotFoundException {
        parseFile(args);
        //parseProf(args);
    }
}