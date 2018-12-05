import java.util.ArrayList;
import java.util.LinkedList;

public class Util {
    public static final byte up = 1;
    public static final byte left = 2;
    public static final byte right = 3;
    public static final byte down = 4;

    public static final byte invalidMove = 5;
    public static final byte playerMove = 6;
    public static final byte boxMove = 7;

    public static final byte empty = ' ';
    public static final byte box = '$';
    public static final byte goal = '.';
    public static final byte boxOnGoal = '*';
    public static final byte player = '@';
    public static final byte playerOnGoal = '+';
    public static final byte wall = '#';
    public static final byte deadZone = 'x';
    public static final byte playerOnDeadZone = '!';

    public static final int bfs = 100;
    public static final int dfs = 101;
    public static final int bAbs = 102;

    static LinkedList<BoardState> boardPool = new LinkedList<>();
    static LinkedList<Pair> pairPool = new LinkedList<>();
    static LinkedList<ArrayList<BoardState>> arrayBoardStatePool = new LinkedList<>();
    static LinkedList<ArrayList<Byte>> arrayBytePool = new LinkedList<>();

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

    static void recycle(Pair pair) {
        pairPool.add(pair);
    }

    static Pair getPair(int x, int y) {
        if (!pairPool.isEmpty()) {
            Pair p = pairPool.poll();
            p.set(x, y);
            return p;
        }
        return new Pair(x, y);
    }

    static void recycle(BoardState state) {
        state.reset();
        boardPool.add(state);
    }

    static BoardState getBoard() {
        if (!boardPool.isEmpty()) {
            return boardPool.poll();
        }
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
        return new ArrayList<>();
    }

    static void recycleAB(ArrayList<Byte> arrayByte) {
        arrayByte.clear();
        arrayBytePool.add(arrayByte);
    }

    static ArrayList<Byte> getArrayByte() {
        if (!arrayBytePool.isEmpty()) {
            return arrayBytePool.poll();
        }
        return new ArrayList<>();
    }
}