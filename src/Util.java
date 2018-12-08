import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class Util {
    public static final byte up = 1;
    public static final byte left = 2;
    public static final byte right = 3;
    public static final byte down = 4;

    public static final byte invalidMove = 5;
    public static final byte playerMove = 6;
    public static final byte boxMove = 7;
    public static final byte invalidBoxMove = 8;

    public static final byte zoneChecking = 10;
    public static final byte zoneCheckingHelper = 11;
    public static final byte noZoneChecking = 12;
    public static final byte deadZone = 13;
    public static final byte updateZone = 14;
    public static final byte liveZone = 15;

    public static final byte empty = ' ';
    public static final byte box = '$';
    public static final byte goal = '.';
    public static final byte boxOnGoal = '*';
    public static final byte player = '@';
    public static final byte playerOnGoal = '+';
    public static final byte wall = '#';
    public static final byte deadSquare = 'x';
    public static final byte playerOnDeadSquare = '!';

    public static final int bfs = 100;
    public static final int dfs = 101;
    public static final int ids = 102;
    public static final int huerisitc = 103;
    public static final int random = 104;

    public static final int hBoxesOnGoal = 200;
    public static final int hToAnyGoal = 201;
    public static final int hSingleGoal = 202;
    public static final int hMoveCost = 203;
    public static final int hMinMatching = 204;

    public static final int hManhattan = 300;
    public static final int hEuclidean = 301;
    public static final int hRealCost = 302;

    public static final int maxValueInt = Integer.MAX_VALUE;
    //public static final int maxValueInt = -1;

    static LinkedList<BoardState> boardPool = new LinkedList<>();
    static LinkedList<Pair> pairPool = new LinkedList<>();
    private static LinkedList<ArrayList<BoardState>> arrayBoardStatePool = new LinkedList<>();
    private static LinkedList<ArrayList<Byte>> arrayBytePool = new LinkedList<>();
    private static LinkedList<PairPairByte> pairPairBytePool = new LinkedList<>();
    private static LinkedList<DoublePair> doublePairPool = new LinkedList<>();
    private static LinkedList<Zone> zonePool = new LinkedList<>();

    private static LinkedList<PriorityQueue<Pair>> priorityQueuePool = new LinkedList<>();
    private static int countBoardPool = 0;
    private static int countZonePool = 0;
    private static int countPairPool = 0;
    private static int countArrayBoardStatePool = 0;
    private static int countArrayBytePool = 0;
    private static int countPairPairBytePool = 0;
    private static int countDoublePairPool = 0;
    private static int countPriorityQueuePool = 0;

    Util() {
    }

    static String byteToString(byte b) {
        switch (b) {
            case up:
                return "Up";
            case left:
                return "Left";
            case right:
                return "Right";
            case down:
                return "Down";
        }
        return "NULL";
    }

    /*
     * BoardState Functions
     */

    public static void setCoordinate(ArrayList<ArrayList<Byte>> board, Pair location, byte slot) {
        board.get(location.getFirst()).set(location.getSecond(), slot);
    }

    public static void setCoordinate(ArrayList<ArrayList<Byte>> board, int x, int y, byte slot) {
        board.get(x).set(y, slot);
    }

    public static byte getCoordinate(ArrayList<ArrayList<Byte>> board, Pair location) {
        return board.get(location.getFirst()).get(location.getSecond());
    }

    public static byte getCoordinate(ArrayList<ArrayList<Byte>> board, int x, int y) {
        return board.get(x).get(y);
    }

    public static byte getCoordinate(ArrayList<ArrayList<Byte>> board, Pair location, int offsetRow, int offsetColumn) {
        return board.get(location.getFirst() + offsetRow).get(location.getSecond() + offsetColumn);
    }

    public static boolean moveBoxExtraHelper(ArrayList<ArrayList<Byte>> board, Pair startLocation, Pair endLocation, byte direction) {
        byte ud1 = getCoordinate(board, startLocation, 0, 1);
        byte ud2 = getCoordinate(board, startLocation, 0, -1);
        byte ud3 = getCoordinate(board, endLocation, 0, 1);
        byte ud4 = getCoordinate(board, endLocation, 0, -1);

        byte lr1 = getCoordinate(board, startLocation, 1, 0);
        byte lr2 = getCoordinate(board, startLocation, -1, 0);
        byte lr3 = getCoordinate(board, endLocation, 1, 0);
        byte lr4 = getCoordinate(board, endLocation, -1, 0);

        switch (direction) {
            case up:
            case down:
                if (!(ud1 == wall && ud2 == wall && ud3 == wall && ud4 == wall)) {
                    return false;
                }
                break;
            case left:
            case right:
                if (!(lr1 == wall && lr2 == wall && lr3 == wall && lr4 == wall)) {
                    return false;
                }
                break;
        }
        return true;
    }

    public static int getOffsetColumn(byte direction) {
        switch (direction) {
            case Util.up:
            case Util.down:
                return 0;
            case Util.right:
                return 1;
            case Util.left:
                return -1;
            default:
                System.out.println("Incorrect Direction");
        }
        return 0;
    }

    public static int getOffsetRow(byte direction) {
        switch (direction) {
            case Util.up:
                return -1;
            case Util.down:
                return 1;
            case Util.right:
            case Util.left:
                return 0;
            default:
                System.out.println("Incorrect Direction");
        }
        return 0;
    }

    /*
     * Pooling Functions
     */

    static void recycle(Pair pair) {
        pairPool.add(pair);
    }

    static Pair getPair(int x, int y) {
        if (!pairPool.isEmpty()) {
            Pair p = pairPool.poll();
            p.set(x, y);
            return p;
        }
        countPairPool++;
        return new Pair(x, y);
    }

    static void recycle(Zone z) {
        z.reset();
        zonePool.add(z);
    }

    static Zone getZone() {
        if (!zonePool.isEmpty()) {
            return zonePool.poll();
        }
        countZonePool++;
        return new Zone();
    }

    static void recycle(PairPairByte pair) {
        pairPairBytePool.add(pair);
    }

    static PairPairByte getPairPairByte() {
        if (!pairPairBytePool.isEmpty()) {
            return pairPairBytePool.poll();
        }
        countPairPairBytePool++;
        return new PairPairByte();
    }

    static void recycle(BoardState state) {
        state.reset();
        boardPool.add(state);
    }

    static BoardState getBoard() {
        if (!boardPool.isEmpty()) {
            return boardPool.poll();
        }
        countBoardPool++;
        return new BoardState();
    }

    static void recycleABS(ArrayList<BoardState> arrayBoard) {
        arrayBoard.clear();
        arrayBoardStatePool.add(arrayBoard);
    }

    static ArrayList<BoardState> getArrayBoardState() {
        if (!arrayBoardStatePool.isEmpty()) {
            return arrayBoardStatePool.poll();
        }
        countArrayBoardStatePool++;
        return new ArrayList<>();
    }

    static PriorityQueue<Pair> getPriorityQueue() {
        if (!priorityQueuePool.isEmpty()) {
            return priorityQueuePool.poll();
        }
        countPriorityQueuePool++;
        return new PriorityQueue<>();

    }

    static void recyclePriority(PriorityQueue<Pair> queue) {
        queue.clear();
        priorityQueuePool.add(queue);
    }

    static void recycleAB(ArrayList<Byte> arrayByte) {
        arrayByte.clear();
        arrayBytePool.add(arrayByte);
    }

    static ArrayList<Byte> getArrayByte() {
        if (!arrayBytePool.isEmpty()) {
            return arrayBytePool.poll();
        }
        countArrayBytePool++;
        return new ArrayList<>();
    }

    static void recycle(DoublePair pair) {
        doublePairPool.add(pair);
    }

    static DoublePair getDoublePair(Pair x, Pair y) {
        if (!doublePairPool.isEmpty()) {
            DoublePair p = doublePairPool.poll();
            p.set(x, y);
            return p;
        }
        countDoublePairPool++;
        return new DoublePair(x, y);
    }

    static int getCountBoardPool() {
        return countBoardPool;
    }

    static int getCountPairPool() {
        return countPairPool;
    }

    static int getCountArrayBoardStatePool() {
        return countArrayBoardStatePool;
    }

    static int getCountArrayBytePool() {
        return countArrayBytePool;
    }

    static int getSizeBoardPool() {
        return boardPool.size();
    }

    static int getSizePairPool() {
        return pairPool.size();
    }

    static int getSizeArrayBoardStatePool() {
        return arrayBoardStatePool.size();
    }

    static int getSizeArrayBytePool() {
        return arrayBytePool.size();
    }

    static int getCountPairPairBytePool() {
        return countPairPairBytePool;
    }

    static int getSizePairPairBytePool() {
        return pairPairBytePool.size();
    }

    static int getCountDoublePairPool() {
        return countDoublePairPool;
    }

    static int getSizeDoublePairPool() {
        return doublePairPool.size();
    }

    static int getCountZonePool() {
        return countZonePool;
    }

    static int getSizeZonePool() {
        return zonePool.size();
    }

    static int getSizePriorityPool() {
        return priorityQueuePool.size();
    }

    static int getCountPriorityQueuePool() {
        return countPriorityQueuePool;
    }
}