package nl.hva.ict.se.sands;

import java.util.*;

/**
 * Given a list of Archer's this class can be used to sort the list using one of three sorting algorithms.
 * Note that you are NOT allowed to change the signature of these methods! Adding method is perfectly fine.
 */
public class ChampionSelector {
    /**
     * This method uses either selection sort or insertion sort for sorting the archers.
     */
    public static List<Archer> selInsSort(List<Archer> archers, Comparator<Archer> scoringScheme) {
        // Looping through the archers list.
        for (int i = 1; i < archers.size(); i++) {
            // Getting the archers indexes that are unsorted.
            Archer archersList = archers.get(i);
            // New variable j equals index i.
            int j = i;
            // While index j is above zero.
            while (j > 0) {
                // Creating right place by moving elements.
                Archer archersList2 = archers.get(j - 1);
                if (scoringScheme.compare(archersList, archersList2) >= 0) {
                    break;
                }
                // Moving elements.
                archers.set(j, archersList2);
                j--;
            }
            // Found the right place and inserting.
            archers.set(j, archersList);
        }
        return archers;
    }

    /**
     * This method uses quick sort for sorting the archers.
     */
    public static List<Archer> quickSort(List<Archer> archers, Comparator<Archer> scoringScheme, int hi, int lo) {
        // If low is higher or equal to high, list is considered too short to sort.
        if (lo >= hi) {
            return archers;
        }

        // Determining the pivot.
        int middle = lo + (hi - lo) / 2;
        Archer pivot = archers.get(middle);

        // Setting the left and right parts, and keep incrementing/decrementing.
        // Comparing to see if current archer is greater or smaller than the pivot.
        int i = lo, j = hi;
        while (i <= j) {
            while (scoringScheme.compare(archers.get(i), pivot) < 0) {
                i++;
            }

            while (scoringScheme.compare(archers.get(j), pivot) > 0) {
                j--;
            }

            // If i is lower or equal to j swap the archers.
            if (i <= j) {
                Archer tempArch = archers.get(i);
                archers.set(i, archers.get(j));
                archers.set(j, tempArch);
                i++;
                j--;
            }
        }

        // Recursion to sort sub parts.
        if (lo < j) {
            quickSort(archers, scoringScheme, j, lo);
        }

        if (hi > i) {
            quickSort(archers, scoringScheme, hi, i);
        }
        return archers;
    }

    /**
     * This method uses the Java collections sort algorithm for sorting the archers.
     */
    public static List<Archer> collectionSort(List<Archer> archers, Comparator<Archer> scoringScheme) {
        Collections.sort(archers, scoringScheme);
        return archers;
    }
}
