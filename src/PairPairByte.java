public class PairPairByte {
    Pair boxLocation = Util.getPair(0,0);
    byte returnType = 0;

    public PairPairByte(){

    }

    public PairPairByte(Pair p, byte b) {
        this.boxLocation.set(p);
        this.returnType = b;
    }

    public void set(Pair p, byte b) {
        this.boxLocation.set(p);
        this.returnType = b;
    }

    @Override
    public String toString() {
        return "(" + boxLocation.toString() + "," + returnType + ")";
    }

}