package nl.hva.ict.se.sands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

public class ComparatorTest {
    private final int TESTS = 10;
    private final int MIN_ARCHERS = 100;
    private final int MAX_ARCHERS = 5000000;
    private final double MAX_SECONDS = 20.000;

    protected Comparator<Archer> comparator;
    // The unsorted list of archers for every test.
    List<Archer> archers;

    @BeforeEach
    public void createComparator() {
        comparator = new Comparator<Archer>() {

            @Override
            public int compare(Archer o1, Archer o2) {
                // If total score is equal then return
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
    public void testingArchersSelInsSort() {
        // Running the experiment 10 times.
        for (int i = 0; i <= TESTS; i++) {
            double duration = 0;

            // Amount of archers starts at 100.
            // Amount of archers stops at 5.0000.000.
            // Amount of archers keeps multiplying with 2.
            for (int amountOfArchers = MIN_ARCHERS;
                 amountOfArchers < MAX_ARCHERS;
                 amountOfArchers *= 2) {

                archers = Archer.generateArchers(amountOfArchers);

                // Defining the time of starting and ending.
                // Selection or insertion sort.
                double start = System.currentTimeMillis();
                ChampionSelector.selInsSort(archers, comparator);
                double end = System.currentTimeMillis();
                duration = (end - start) / 1000;

                // Break the loop if duration hits 20 seconds.
                if (duration >= MAX_SECONDS) {
                    break;
                }

                System.out.printf("SEL/INS: Sorted %d archers in %.2f seconds%n", amountOfArchers, duration);
            }
        }
    }

    @Test
    public void testingArchersQuickSort() {
        // Running the experiment 10 times.
        for (int i = 0; i < TESTS; i++) {
            double duration = 0;

            // Amount of archers starts at 100.
            // Amount of archers stops at 5.0000.000.
            // Amount of archers keeps multiplying with 2.
            for (int amountOfArchers = MIN_ARCHERS;
                 amountOfArchers < MAX_ARCHERS;
                 amountOfArchers *= 2) {

                archers = Archer.generateArchers(amountOfArchers);

                // Defining the time of starting and ending.
                // Quick sort.
                double start = System.currentTimeMillis();
                ChampionSelector.quickSort(archers, comparator, archers.size() - 1, 0);
                double end = System.currentTimeMillis();
                duration = (end - start) / 1000;

                // Break the loop if duration hits 20 seconds.
                if (duration >= MAX_SECONDS) {
                    break;
                }
                System.out.printf("QUICK: Sorted %d archers in %.2f seconds%n", amountOfArchers, duration);
            }
        }
    }

    @Test
    public void testingArchersCollectionSort() {
        // Running the experiment 10 times.
        for (int i = 0; i < TESTS; i++) {
            double duration = 0;

            // Amount of archers starts at 100.
            // Amount of archers stops at 5.0000.000.
            // Amount of archers keeps multiplying with 2.
            for (int amountOfArchers = MIN_ARCHERS;
                 amountOfArchers < MAX_ARCHERS;
                 amountOfArchers *= 2) {

                archers = Archer.generateArchers(amountOfArchers);

                // Defining the time of starting and ending.
                // Collection sort.
                double start = System.currentTimeMillis();
                ChampionSelector.collectionSort(archers, comparator);
                double end = System.currentTimeMillis();
                duration = (end - start) / 1000;

                // Break the loop if duration hits 20 seconds.
                if (duration >= MAX_SECONDS) {
                    break;
                }
                System.out.printf("COLLECTION: Sorted %d archers in %.2f seconds%n", amountOfArchers, duration);
            }
        }
    }
}
