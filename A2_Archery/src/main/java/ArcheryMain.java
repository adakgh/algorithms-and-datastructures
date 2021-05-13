import nl.hva.ict.se.sands.Archer;
import nl.hva.ict.se.sands.ChampionSelector;

import java.util.Comparator;
import java.util.List;

public class ArcheryMain {

    public static void main(String[] args) {
        // Test output.
        System.out.println("Welcome to the HvA Archery competition");

        // Generating test archers.
        List<Archer> archers = Archer.generateArchers(5);
        System.out.println("Unsorted archers: " + archers + "\n");

        Comparator<Archer> comp = (o1, o2) -> {
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
        };

        // Printing archers sorted by selection or insertion sort.
        ChampionSelector.selInsSort(archers, comp);
        System.out.println("Archers sorted by selection or insertion sort: " + archers + "\n");

        // Printing archers sorted by quick sort.
        ChampionSelector.quickSort(archers, comp, archers.size() - 1, 0);
        System.out.println("Archers sorted by quick sort: " + archers + "\n");

        // Printing archers sorted by collection sort.
        ChampionSelector.collectionSort(archers, comp);
        System.out.println("Archers sorted by Collections sort: " + archers + "\n");
    }
}
