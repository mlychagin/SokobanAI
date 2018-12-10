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

    public static void statLeak() {
        int countBP = Util.getCountBoardPool();
        int sizeBP = Util.getSizeBoardPool();

        int countPP = Util.getCountPairPool();
        int sizePP = Util.getSizePairPool();

        int countABSP = Util.getCountArrayBoardStatePool();
        int sizeABSP = Util.getSizeArrayBoardStatePool();

        int countABP = Util.getCountArrayBytePool();
        int sizeABP = Util.getSizeArrayBytePool();

        int countPPBP = Util.getCountPairPairBytePool();
        int sizePPBP = Util.getSizePairPairBytePool();

        int countDPP = Util.getCountDoublePairPool();
        int sizeDPP = Util.getSizeDoublePairPool();

        int countZP = Util.getCountZonePool();
        int sizeZP = Util.getSizeZonePool();

        int countPQP = Util.getCountPriorityQueuePool();
        int sizePQP = Util.getSizePriorityPool();

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
        engine.findSolution(solution, Util.ids, Util.hMinMatching, Util.hManhattan, true);
        engine.cleanUpAll(Util.heuristic);
        printSolution(solution);
        Util.recycleAB(solution);
        statLeak();
    }

    public static boolean checkSolution(GameEngine engine, ArrayList<Byte> solution, ArrayList<String> inputMap) {
        engine.initHelper(inputMap);
        BoardState root = engine.root;
        Collections.reverse(solution);
        for (byte b : solution) {
            PairPairByte ret = root.move(engine.board, b);
            if (ret.returnType == Util.invalidBoxMove || ret.returnType == Util.invalidMove) {
                return false;
            }
            Util.recycle(ret);
        }
        return engine.isGoalState(root);
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
                long lStartTime = System.currentTimeMillis();

                System.out.print(line.substring(1) + ",");
                GameEngine engine = new GameEngine();
                engine.initFull(inputMap);
                ArrayList<Byte> solution = Util.getArrayByte();
                engine.findSolution(solution, Util.ids, Util.hMinMatching, Util.hManhattan, true);

                long lEndTime = System.currentTimeMillis();
                long output = lEndTime - lStartTime;

                System.out.print(output + ",");

                System.out.println(checkSolution(engine, solution, inputMap));

                engine.cleanUpAll(Util.heuristic);
                Util.recycleAB(solution);
                inputMap.clear();
            } else {
                inputMap.add(line);
            }
        }
        statLeak();
    }

    public static void main(String args[]) throws FileNotFoundException {
        parseFile(args);
        //parseProf(args);
    }
}