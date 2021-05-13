package models;

/**
 * Wagon class
 */
public abstract class Wagon {
    public int id;                  // some unique ID of a Wagon
    private Wagon nextWagon;        // another wagon that is appended at the tail of this wagon
    // a.k.a. the successor of this wagon in a sequence
    // set to null if no successor is connected
    private Wagon previousWagon;    // another wagon that is prepended at the front of this wagon
    // a.k.a. the predecessor of this wagon in a sequence
    // set to null if no predecessor is connected


    // representation invariant propositions:
    // tail-connection-invariant:   wagon.nextWagon == null or wagon == wagon.nextWagon.previousWagon
    // front-connection-invariant:  wagon.previousWagon == null or wagon = wagon.previousWagon.nextWagon

    public Wagon(int wagonId) {
        this.id = wagonId;
    }

    public int getId() {
        return id;
    }

    public Wagon getNextWagon() {
        return nextWagon;
    }

    public void setNextWagon(Wagon nextWagon) {
        this.nextWagon = nextWagon;
    }

    public Wagon getPreviousWagon() {
        return previousWagon;
    }

    public void setPreviousWagon(Wagon previousWagon) {
        this.previousWagon = previousWagon;
    }

    /**
     * @return whether this wagon has a wagon appended at the tail
     */
    public boolean hasNextWagon() {
        // return if next wagon is not null
        return (nextWagon != null);
    }

    /**
     * @return whether this wagon has a wagon prepended at the front
     */
    public boolean hasPreviousWagon() {
        // return if previous wagon is not null
        return (previousWagon != null);
    }

    /**
     * finds the last wagon of the sequence of wagons attached to this wagon
     * if no wagons are attached return this wagon itselves
     *
     * @return the wagon found
     */
    public Wagon getLastWagonAttached() {
        Wagon currentWagon = this;

        // while the current wagon still has a next wagon then assign current wagon to the last next wagon
        while (currentWagon.hasNextWagon()) {
            currentWagon = currentWagon.getNextWagon();
        }

        return currentWagon;
    }

    /**
     * @return the number of wagons appended to this wagon
     * return 1 if no wagons have been appended.
     */
    public int getSequenceLength() {
        int length = 1;
        Wagon wagon = this;

        // if there is no next wagon return 1
        if (nextWagon == null) {
            return length;
        }

        // while there is a next wagon get the next wagons and count up the length
        while (wagon.hasNextWagon()) {
            wagon = wagon.getNextWagon();
            length++;
        }

        return length;
    }

    /**
     * attaches this wagon at the tail of a given prevWagon.
     *
     * @param newPreviousWagon the previous wagon that is getting an attachment.
     * @throws RuntimeException if this wagon already has been appended to a wagon.
     * @throws RuntimeException if prevWagon already has got a wagon appended.
     */
    public void attachTo(Wagon newPreviousWagon) throws RuntimeException {
        // if the wagon has not already been attached and prevWagon does not have a wagon appended
        // set the current wagon as the next wagon of the given previous wagon
        // then set the given previous wagon as the previous wagon
        // else throw exception
        Wagon wagon = this;
        if (!wagon.hasPreviousWagon() || !newPreviousWagon.hasNextWagon()) {
            newPreviousWagon.setNextWagon(this);
            this.setPreviousWagon(newPreviousWagon);
        } else {
            throw new RuntimeException();
        }
    }

    /**
     * detaches this wagon from its previous wagons.
     * no action if this wagon has no previous wagon attached.
     */
    public void detachFromPrevious() {
        // if current wagon has a previous wagon then get the previous wagon and set the next wagon as null
        if (this.hasPreviousWagon()) {
            getPreviousWagon().setNextWagon(null);
            // also remove the previous wagon
            setPreviousWagon(null);
        }
    }

    /**
     * detaches this wagon from its tail wagons.
     * no action if this wagon has no succeeding next wagon attached.
     */
    public void detachTail() {
        // detaching last wagon by setting previous wagon as null
        getNextWagon().setPreviousWagon(null);

        // set the next wagon as null so there are no more wagons coming up next
        setNextWagon(null);
    }

    /**
     * attaches this wagon at the tail of a given newPreviousWagon.
     * if required, first detaches this wagon from its current predecessor
     * and/or detaches the newPreviousWagon from its current successor
     *
     * @param newPreviousWagon the previous wagon that is getting a reattachment.
     */
    public void reAttachTo(Wagon newPreviousWagon) {
        // detaching the predecessor of the wagon and successor of the newPreviousWagon
        Wagon wagon = this;

        if (getPreviousWagon() != null)
            wagon.getPreviousWagon().setNextWagon(null);

        newPreviousWagon.setNextWagon(this);

        // making new attachment by setting the previous wagon that is getting a reattachment as the previous wagon
        setPreviousWagon(newPreviousWagon);
    }

    /**
     * Removes this wagon from the sequence that it is part of, if any.
     * Reconnect the subsequence of its predecessors with the subsequence of its successors, if any.
     */
    public void removeFromSequence() {
        // remove wagon and reattach if wagon has next and previous wagon
        // detach wagon from previous if there is a previous wagon but no next wagon
        // detail tail if there is a next wagon but no previous wagon
        if (hasNextWagon() && hasPreviousWagon()) {
            getNextWagon().reAttachTo(getPreviousWagon());

            setNextWagon(null);
            setPreviousWagon(null);
        } else if (hasPreviousWagon() && !hasNextWagon()) {
            detachFromPrevious();
        } else if (!hasPreviousWagon() && hasNextWagon()) {
            detachTail();
        }
    }

    /**
     * reverses the order in the sequence of wagons from this Wagon until its final successor.
     * The reversed sequence is attached again to the predecessor of this Wagon, if any.
     * no action if this Wagon has no succeeding next wagon attached.
     *
     * @return the new start Wagon of the reversed sequence (with is the former last Wagon of the original sequence)
     */
    public Wagon reverseSequence() {
        Wagon nextWagon;
        Wagon wagon = this;
        Wagon previousWagon = this.getPreviousWagon();

        // sequence is attached to predecessor of this wagon if found
        if (wagon.getPreviousWagon() != null) {
            wagon.getNextWagon().setNextWagon(wagon);
            wagon.getNextWagon().setPreviousWagon(previousWagon);
            previousWagon.setNextWagon(wagon.getNextWagon());
            wagon.setPreviousWagon(wagon.getNextWagon());
            wagon.setNextWagon(null);

            return previousWagon.getNextWagon();
        }

        // walking through the wagons and reversing the order
        while (wagon != null) {
            nextWagon = wagon.getNextWagon();
            wagon.setNextWagon(previousWagon);
            wagon.setPreviousWagon(nextWagon);
            previousWagon = wagon;
            wagon = nextWagon;
        }

        return previousWagon;
    }

    @Override
    public String toString() {
        // print out the wagon id
        return String.format("[Wagon-%d]", id);
    }
}
