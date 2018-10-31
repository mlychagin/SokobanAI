public class BoardState {
    private byte[][] board;
    private int row;
    private int column;

    public BoardState(int height, int width){
        board = new byte[height][width];
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                board[i][j] = Util.empty;
            }
        }
    }

    public BoardState(byte[][] board){
        this.board = board;
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board[i].length; j++){
                if(board[i][j] == 4){
                    row = i;
                    column = j;
                    return;
                }
            }
        }
    }

    public void setPlayerLocation(int x, int y){
        row = x-1;
        column = y-1;
        board[row][column] = Util.player;
    }

    public void setWalls(int[] walls){
        for(int i = 0; i < walls.length; i += 2){
            board[walls[i]-1][walls[i+1]-1] = Util.wall;
        }
    }

    public void setBoxes(int[] boxes){
        for(int i = 0; i < boxes.length; i += 2){
            board[boxes[i]-1][boxes[i+1]-1] = Util.box;
        }
    }

    public void setGoals(int[] goals){
        for(int i = 0; i < goals.length; i += 2){
            board[goals[i]-1][goals[i+1]-1] = Util.goal;
        }
    }

    private boolean moveBox(int startRow, int startColumn, int endRow, int endColumn){
        switch(board[endRow][endColumn]){
            case Util.empty:
                if(board[startRow][startColumn] == Util.box){
                    board[startRow][startColumn] = Util.empty;
                }
                if(board[startRow][startColumn] == Util.boxOnGoal){
                    board[startRow][startColumn] = Util.goal;
                }
                board[endRow][endColumn] = Util.box;
                return true;
            case Util.goal:
                board[endRow][endColumn] = Util.boxOnGoal;
                return true;
            default:
                return false;
        }
    }

    public void updatePlayerPositionAfterMoving(){
        if(board[row][column] == Util.playerOnGoal){
            board[row][column] = Util.box;
        } else {
            board[row][column] = Util.empty;
        }
    }

    public boolean move(byte direction){
        byte offsetRow = 0;
        byte offsetColumn = 0;
        switch (direction){
            case Util.up:
                offsetRow = -1;
                break;
            case Util.down:
                offsetRow = 1;
                break;
            case Util.right:
                offsetColumn = 1;
                break;
            case Util.left:
                offsetColumn = -1;
                break;
        }
        switch (board[row+offsetRow][column+offsetColumn]){
            case Util.empty:
                board[row+offsetRow][column+offsetColumn] = Util.player;
                break;
            case Util.box:
            case Util.boxOnGoal:
                if(!moveBox(row+offsetRow, column+offsetColumn, row+offsetRow*2, column+offsetColumn*2)){
                    return false;
                }
                break;
            case Util.goal:
                board[row+offsetRow][column+offsetColumn] = Util.playerOnGoal;
                break;
            default:
                return false;
        }
        updatePlayerPositionAfterMoving();
        switch (direction){
            case Util.up:
                row--;
                break;
            case Util.down:
                row++;
                break;
            case Util.right:
                column++;
                break;
            case Util.left:
                column--;
                break;
        }
        if(board[row][column] == Util.boxOnGoal){
            board[row][column] = Util.playerOnGoal;
        } else {
            board[row][column] = Util.player;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (byte[] row : board) {
            for (byte column : row) {
                switch (column){
                    case Util.empty:
                        builder.append(" ");
                        break;
                    case Util.box:
                        builder.append("$");
                        break;
                    case Util.goal:
                        builder.append(".");
                        break;
                    case Util.boxOnGoal:
                        builder.append("*");
                        break;
                    case Util.player:
                        builder.append("@");
                        break;
                    case Util.playerOnGoal:
                        builder.append("+");
                        break;
                    case Util.wall:
                        builder.append("#");
                        break;
                }
                builder.append(" ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}
