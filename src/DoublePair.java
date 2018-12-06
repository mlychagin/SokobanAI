public class DoublePair {
    private Pair playerLocation;
    private Pair boxLocation;

    public DoublePair(Pair playerLocation, Pair boxLocation) {
        this.playerLocation = playerLocation;
        this.boxLocation = boxLocation;
    }

    public Pair getPlayerLocation() {
        return playerLocation;
    }

    public Pair getBoxLocation() {
        return boxLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DoublePair that = (DoublePair) o;

        if (playerLocation != null ? !playerLocation.equals(that.playerLocation) : that.playerLocation != null)
            return false;
        return boxLocation != null ? boxLocation.equals(that.boxLocation) : that.boxLocation == null;
    }

    @Override
    public int hashCode() {
        int result = playerLocation != null ? playerLocation.hashCode() : 0;
        result = 31 * result + (boxLocation != null ? boxLocation.hashCode() : 0);
        return result;
    }
}


