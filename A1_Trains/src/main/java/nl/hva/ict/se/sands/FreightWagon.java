package models;

/**
 * Freight wagon class
 */
public class FreightWagon extends Wagon {
    public int maxWeight;

    public FreightWagon(int wagonId, int maxWeight) {
        super(wagonId);
        this.maxWeight = maxWeight;
    }

    public int getMaxWeight() {
        return maxWeight;
    }
}
