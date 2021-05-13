import java.util.Comparator;

public class PatientComparator implements Comparator<Patient> {
    @Override
    public int compare(Patient o1, Patient o2) {
        if (o1.isHasPriority() && !o2.isHasPriority()) {
            return -1; // if patient 1 has a priority: push patient up the queue
        } else if (o2.isHasPriority() && !o1.isHasPriority()) {
            return 1;  // if patient 1 does not have a priority: push patient down the queue
        } else {
            return o1.getArrivedAt().compareTo(o2.getArrivedAt());
        }
    }
}
