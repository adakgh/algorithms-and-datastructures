package nl.hva.ict.se.sands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChampionSelectorTest {
    protected Comparator<Archer> comparator;

    @BeforeEach
    public void createComparator() {
        comparator = new Comparator<Archer>() {

            @Override
            public int compare(Archer o1, Archer o2) {
                // If total score is equal then compare amount of 10's and/or 9's.
                if (o2.getTotalScore() == o1.getTotalScore()) {
                    if (o2.getTens() == o1.getTens()) {
                        if (o2.getNines() == o1.getNines()) {
                            // If the total scores are equal, and amount of tens and nines are equal,
                            // the champion is the archer with the higher id (less experienced) wins.
                            return o2.getId() - o1.getId();
                        }
                        // If the total scores are equal, and amount of tens are equal,
                        // the champion is the archer with the most nines.
                        return o2.getNines() - o1.getNines();
                    }
                    // If the total scores are equal, the champion is the archer with the most tens.
                    return o2.getTens() - o1.getTens();
                }
                // The champion is the archer with the highest total score.
                return o2.getTotalScore() - o1.getTotalScore();
            }
        };
    }

    @Test
    public void selInsSortAndCollectionSortResultInSameOrder() {
        List<Archer> unsortedArchersForSelIns = Archer.generateArchers(23);
        List<Archer> unsortedArchersForCollection = new ArrayList<>(unsortedArchersForSelIns);

        List<Archer> sortedArchersSelIns = ChampionSelector.selInsSort(unsortedArchersForSelIns, comparator);
        List<Archer> sortedArchersCollection = ChampionSelector.collectionSort(unsortedArchersForCollection, comparator);

        assertEquals(sortedArchersCollection, sortedArchersSelIns);
    }

    @Test
    public void quickSortAndCollectionSortResultInSameOrder() {
        List<Archer> unsortedArchersForQuickSort = Archer.generateArchers(25);
        List<Archer> unsortedArchersForCollection = new ArrayList<>(unsortedArchersForQuickSort);

        List<Archer> sortedArchersQuickSort = ChampionSelector.quickSort(unsortedArchersForQuickSort, comparator, unsortedArchersForQuickSort.size() - 1, 0);
        List<Archer> sortedArchersCollection = ChampionSelector.collectionSort(unsortedArchersForCollection, comparator);

        assertEquals(sortedArchersCollection, sortedArchersQuickSort);
    }

}
