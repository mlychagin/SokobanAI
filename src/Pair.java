public class Pair {
    int first;
    int second;

    public Pair(int first, int second) {
        this.first = first;
        this.second = second;
    }

    public Pair(Pair p) {
        this.first = p.getFirst();
        this.second = p.getSecond();
    }

    public void set(Pair p) {
        first = p.first;
        second = p.second;
    }

    public void set(int first, int second) {
        this.first = first;
        this.second = second;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getFirst() {
        return first;
    }

    public int getSecond() {
        return second;
    }

    public Pair clonePair() {
        return Util.getPair(first, second);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair pair = (Pair) o;

        if (first != pair.first) return false;
        return second == pair.second;
    }

    @Override
    public int hashCode() {
        int result = first;
        result = 31 * result + second;
        return result;
    }
}