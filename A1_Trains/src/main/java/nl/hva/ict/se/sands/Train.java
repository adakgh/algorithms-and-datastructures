package models;

import java.util.Iterator;

/**
 * Train class
 */
public class Train implements Iterable<Wagon> {
    private String origin;
    private String destination;
    private Locomotive engine;
    private Wagon firstWagon;

    /* Representation invariants:
        firstWagon == null || firstWagon.previousWagon == null
        engine != null
     */

    public Train(Locomotive engine, String origin, String destination) {
        this.engine = engine;
        this.destination = destination;
        this.origin = origin;
    }

    /* three helper methods that are useful in other methods */
    public boolean hasWagons() {
        return firstWagon != null;
    }

    public boolean isPassengerTrain() {
        // check if the first wagon is a passenger wagon
        return firstWagon instanceof PassengerWagon;
    }

    public boolean isFreightTrain() {
        // check if the first wagon is a freight wagon
        return firstWagon instanceof FreightWagon;
    }

    public Locomotive getEngine() {
        return engine;
    }

    public Wagon getFirstWagon() {
        return firstWagon;
    }

    /**
     * Replaces the current sequence of wagons (if any) in the train
     * by the given new sequence of wagons (if any)
     * (sustaining all representation invariants)
     *
     * @param newSequence the new sequence of wagons (can be null)
     */
    public void setFirstWagon(Wagon newSequence) {
        // setting the previous wagon as null and first wagon as the new sequence
        newSequence.setPreviousWagon(null);
        this.firstWagon = newSequence;
    }

    /**
     * @return the number of Wagons connected to the train
     */
    public int getNumberOfWagons() {
        // if there are wagons get the sequence length of the wagons
        int numberOfWagons = 0;

        if (hasWagons()) {
            numberOfWagons = getFirstWagon().getSequenceLength();
        }

        return numberOfWagons;
    }

    /**
     * @return the last wagon attached to the train
     */
    public Wagon getLastWagonAttached() {
        // if train has wagons
        if (hasWagons() && getNumberOfWagons() > 1) {
            // looping through the wagons till there are no more, return the last wagon
            for (Wagon currentWagon : this) {
                if (!currentWagon.hasNextWagon()) {
                    return currentWagon;
                }
            }
            // if there is only 1 wagon get automatically the first wagon
        } else if (getNumberOfWagons() == 1) {
            return getFirstWagon();
        }

        return null;
    }

    /**
     * @return the total number of seats on a passenger train
     * (return 0 for a freight train)
     */
    public int getTotalNumberOfSeats() {
        int counter = 0;

        // only count number of seats if it is a passenger train
        if (this.isPassengerTrain()) {
            for (Wagon w : this) {
                PassengerWagon wagon = (PassengerWagon) w;
                counter += wagon.getNumberOfSeats();
            }
        }
        return counter;
    }

    /**
     * calculates the total maximum weight of a freight train
     *
     * @return the total maximum weight of a freight train
     * (return 0 for a passenger train)
     */
    public int getTotalMaxWeight() {
        int weight = 0;

        // only count weight if it is a freight train
        if (firstWagon instanceof FreightWagon) {
            for (Wagon wagon : this) {
                weight += ((FreightWagon) wagon).getMaxWeight();
            }
            return weight;
        }

        return 0;
    }

    /**
     * Finds the wagon at the given position (starting at 1 for the first wagon of the train)
     *
     * @param position given position
     * @return the wagon found at the given position
     * (return null if the position is not valid for this train)
     */
    public Wagon findWagonAtPosition(int position) {
        Wagon currentWagon = firstWagon;
        int numberOfWagonsFound = 1;

        // if there is no first wagon then return null
        if (firstWagon == null) {
            return null;
        }

        // looping through all wagons and counting the wagons
        for (int i = 1; i < position; i++) {
            if (currentWagon.hasNextWagon()) {
                currentWagon = currentWagon.getNextWagon();
                numberOfWagonsFound++;
            }
        }

        // if the number of wagon match with position return the wagon
        if (numberOfWagonsFound == position) {
            return currentWagon;
        }

        return null;
    }

    /**
     * Finds the wagon with a given wagonId
     *
     * @param wagonId id of the wagon
     * @return the wagon found
     * (return null if no wagon was found with the given wagonId)
     */
    public Wagon findWagonById(int wagonId) {
        Wagon wagon = firstWagon;

        // if the id and wagonId match then return the wagon
        while (wagon != null) {
            if (wagon.getId() == wagonId) {
                return wagon;
            } else {
                wagon = wagon.getNextWagon();
            }
        }
        return null;
    }

    /**
     * Determines if the given sequence of wagons can be attached to the train
     * Verfies of the type of wagons match the type of train (Passenger or Freight)
     * Verfies that the capacity of the engine is sufficient to pull the additional wagons
     *
     * @param sequence wagons that are going to be attached
     * @return whether the attachment could be completed successfully
     */
    public boolean canAttach(Wagon sequence) {
        boolean possibleToAttach = true;

        // verifying if the two types of wagons match
        if (isPassengerTrain() && sequence instanceof FreightWagon) {
            possibleToAttach = false;
        } else if (isFreightTrain() && sequence instanceof PassengerWagon) {
            possibleToAttach = false;

            // verifying capacity of the engine by counting up the wagons of the sequence and the firstwagon
        } else if ((getFirstWagon().getSequenceLength() + sequence.getSequenceLength()) > getEngine().getMaxWagons()) {
            possibleToAttach = false;

            // verifying if id is not the same
        } else if (getFirstWagon().getId() == sequence.getId()) {
            possibleToAttach = false;

        }

        return possibleToAttach;
    }

    /**
     * Tries to attach the given sequence of wagons to the rear of the train
     * No change is made if the attachment cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity)
     *
     * @param sequence wagons that are going to be attached to the rear
     * @return whether the attachment could be completed successfully
     */
    public boolean attachToRear(Wagon sequence) {
        // if types of train and wagon do not match return false
        if (getFirstWagon() instanceof PassengerWagon && sequence instanceof FreightWagon) {
            return false;
        }

        // if types of train and wagon do not match return false
        if (getFirstWagon() instanceof FreightWagon && sequence instanceof PassengerWagon) {
            return false;
        }

        // if there are already a maximum of wagons return false
        if (this.engine.getMaxWagons() <= this.getNumberOfWagons()) {
            return false;
        }

        // if there are no wagons make the sequence the first wagon
        if (!this.hasWagons()) {
            this.firstWagon = sequence;
            this.firstWagon.setPreviousWagon(null);
            return true;
        }

        // attaching the sequence to another wagon
        Wagon wagon = this.firstWagon;
        while (wagon.hasNextWagon()) {
            wagon = wagon.getNextWagon();
        }
        sequence.attachTo(wagon);
        return true;
    }

    /**
     * Tries to insert the given sequence of wagons at the front of the train
     * No change is made if the insertion cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity)
     *
     * @param sequence wagons that are inserted at the front of the train
     * @return whether the insertion could be completed successfully
     */
    public boolean insertAtFront(Wagon sequence) {
        // if the trains has no wagons set the first wagon as the sequence
        if (!hasWagons()) {
            setFirstWagon(sequence);
            return true;
        } else if (canAttach(sequence)) {
            // get last wagon and set the first wagon to be the last wagon
            sequence.getLastWagonAttached().setNextWagon(getFirstWagon());
            // connect the last wagon to the end of the sequence
            getFirstWagon().setPreviousWagon(sequence.getLastWagonAttached());
            sequence.setPreviousWagon(null);
            // set the new first wagon
            setFirstWagon(sequence);
            return true;
        }
        return false;
    }

    /**
     * Tries to insert the given sequence of wagons at the given wagon position in the train
     * No change is made if the insertion cannot be made.
     * (when the sequence is not compatible of the engine has insufficient capacity
     * or the given position is not valid in this train)
     *
     * @param position place in the train
     * @param sequence wagons that are inserted at the given position
     * @return whether the insertion could be completed successfully
     */
    public boolean insertAtPosition(int position, Wagon sequence) {
        if (!hasWagons()) {
            // if train is empty and the position is at the front of the train
            if (position == 1) {
                setFirstWagon(sequence);
                return true;
            } else {
                return false;
            }
        }

        if (canAttach(sequence)) {
            // get wagon on that position
            Wagon wagon = findWagonAtPosition(position);
            // attach wagon to the last wagon of sequence
            sequence.getLastWagonAttached().setNextWagon(wagon);

            // connect the wagon to the last wagon of the sequence
            if (wagon != null) {
                wagon.setPreviousWagon(sequence);
            }
            return true;
        }

        return false;
    }

    /**
     * Tries to remove one Wagon with the given wagonId from this train
     * and attach it at the rear of the given toTrain
     * No change is made if the removal or attachment cannot be made
     * (when the wagon cannot be found, or the trains are not compatible
     * or the engine of toTrain has insufficient capacity)
     *
     * @param wagonId id of the wagon
     * @param toTrain where the wagons are going to be moved
     * @return whether the move could be completed successfully
     */
    public boolean moveOneWagon(int wagonId, Train toTrain) {
        Wagon wagon = findWagonById(wagonId);

        // if the wagon can be found and canAttach is true
        // then remove the wagon from the train and attach it to the rear
        if (wagon != null) {
            if (canAttach(wagon)) {
                wagon.removeFromSequence();
                toTrain.attachToRear(wagon);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Tries to split this train and move the complete sequence of wagons from the given position
     * to the rear of toTrain
     * No change is made if the split or re-attachment cannot be made
     * (when the position is not valid for this train, or the trains are not compatible
     * or the engine of toTrain has insufficient capacity)
     *
     * @param position place in the train
     * @param toTrain  where the wagons are going to be moved
     * @return whether the move could be completed successfully
     */
    public boolean splitAtPosition(int position, Train toTrain) {
        Wagon wagon = findWagonAtPosition(position);

        // if wagon can be found
        // split train and move the sequence from position to rear
        if (wagon != null) {
            wagon.detachFromPrevious();
            if (canAttach(wagon)) {
                toTrain.attachToRear(wagon);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Reverses the sequence of wagons in this train (if any)
     * i.e. the last wagon becomes the first wagon
     * the previous wagon of the last wagon becomes the second wagon
     * etc.
     * (No change if the train has no wagons or only one wagon)
     */
    public void reverse() {
        if (!this.hasWagons() || this.firstWagon.getNextWagon() == null) {
        } else {

            // Reverses the wagons of the train, 2 variables to set a temporary wagon and the current wagon
            Wagon temp;
            Wagon current = firstWagon;

            // While the current wagon is not at the end, go through each wagon and reverse the wagons of the train
            while (current != null) {
                temp = current.getPreviousWagon();
                current.setPreviousWagon(current.getNextWagon());
                current.setNextWagon(temp);
                current = current.getPreviousWagon();

                if (temp != null) {
                    firstWagon = temp.getPreviousWagon();
                }
            }
        }
    }

    public Iterator<Wagon> iterator() {
        return new Iterator<>() {
            Wagon current = firstWagon;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Wagon next() {
                Wagon prev = current;
                current = current.getNextWagon();

                return prev;
            }
        };
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(engine.toString());
        Wagon next = this.getFirstWagon();

        while (next != null) {
            result.append(next.toString());
            next = next.getNextWagon();
        }

        // printing result
        result.append(String.format(" with %d wagons from %s to %s", getNumberOfWagons(), origin, destination));

        return result.toString();
    }
}
