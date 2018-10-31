public class PlayGame {
    public static void main (String args[]){
        int sizeH = 3;
        int sizeV = 5;
        int[] nWallSquares = {1, 1, 1, 2, 1, 3, 2, 1, 2, 3, 3, 1, 3, 3, 4, 1, 4, 3, 5, 1, 5, 2, 5, 3};
        int[] boxes = {3, 2};
        int[] nStorageLocations = {4, 2};
        int[] initLocal = {2, 2};

        BoardState board = new BoardState(sizeV, sizeH);
        board.setWalls(nWallSquares);
        board.setBoxes(boxes);
        board.setGoals(nStorageLocations);
        board.setPlayerLocation(initLocal[0], initLocal[1]);

        GameEngine engine = new GameEngine(board);
        System.out.println(engine.board.toString());

        System.out.println("Up");
        engine.board.move(Util.up);
        System.out.println(engine.board.toString());
        System.out.println("Right");
        engine.board.move(Util.right);
        System.out.println(engine.board.toString());
        System.out.println("Left");
        engine.board.move(Util.left);
        System.out.println(engine.board.toString());
        System.out.println("Down");
        engine.board.move(Util.down);
        System.out.println(engine.board.toString());

    }
}
