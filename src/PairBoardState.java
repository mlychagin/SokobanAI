public class PairBoardState implements Comparable<PairBoardState>
{
  int key;
  BoardState boardState;

  public PairBoardState(int key, BoardState boardState)
  {
    this.key = key;
    this.boardState = boardState;
  }

  public void set(PairBoardState p)
  {
    key = p.key;
    boardState = p.boardState;
  }

  public void set(int key, BoardState boardState)
  {
    this.key = key;
    this.boardState = boardState;
  }

  public void setKey(int key)
  {
    this.key = key;
  }

  public void setBoardState(BoardState boardState)
  {
    this.boardState = boardState;
  }

  public int getKey()
  {
    return key;
  }

  public BoardState getBoardState()
  {
    return boardState;
  }


  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PairBoardState pair = (PairBoardState) o;

    if (key != pair.key) return false;
    return boardState == pair.boardState;
  }

  @Override
  public int hashCode()
  {
    int result = key;
    result = 31 * result + boardState.hashCode();
    return result;
  }

  @Override
  public int compareTo(PairBoardState o)
  {
    if (this.key > o.key)
    {
      return 1;
    }
    else if (this.key < o.key)
    {
      return -1;
    }
    else
    {
      return 0;
    }
  }
}


