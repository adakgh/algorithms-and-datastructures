import java.time.LocalTime;
import java.util.Random;

public class Nurse implements Comparable<Nurse>  {
    private final static int SAMPLE_TIME_MIN = 60;
    private final static int SAMPLE_TIME_MAX = 160;

    private final String name;
    private LocalTime availableAt;      // the earliest time when this nurse will be available (after finishing a current patient)
    private int numPatientsSampled;     // the total number of patients this nurse has sampled today.
    private int totalSamplingTime;      // the total time spend (in seconds) by this nurse in sampling patients
    private double averageSampleTime;   // the average sample time of regular patients today
    private int workLoad;               // the fraction of the workday the nurse has been busy

    private final Random randomizer;

    /**
     * Creates a new Nurse record for simulating and tracking sampling of patients at the Test Lane
     * @param name          some arbitrary name for reporting
     * @param startTime     The start time of the work day; the earlies time available for the next patient
     * @param randomizer    used to generate reproducible simulation results
     */
    public Nurse(String name, LocalTime startTime, Random randomizer) {
        this.name = name;
        this.availableAt = startTime;
        this.numPatientsSampled = 0;
        this.totalSamplingTime = 0;
        this.randomizer = randomizer;
    }

    /**
     * Handle the sampling of the given patient
     * register all related simulation results as required later for reporting
     * @param patient   the patient to be sampled
     * @param startTime the time at the start of the sampling of the patient
     */
    public void samplePatient(Patient patient, LocalTime startTime) {
        // determine the time needed to complete the sampling procedure
        // this time varies between 60 and 159 seconds as is advised from GGD experience.
        int sampleDuration = SAMPLE_TIME_MIN + randomizer.nextInt(SAMPLE_TIME_MAX - SAMPLE_TIME_MIN);

        patient.setSampledBy(this);
        patient.setSampledAt(startTime);
        this.setTotalSamplingTime(this.getTotalSamplingTime() + sampleDuration);
        this.setAvailableAt(startTime.plusSeconds(sampleDuration));
        this.setNumPatientsSampled(this.getNumPatientsSampled() + 1);
    }

    public LocalTime getAvailableAt() {
        return availableAt;
    }

    public void setAvailableAt(LocalTime availableAt) {
        this.availableAt = availableAt;
    }

    public int getNumPatientsSampled() {
        return numPatientsSampled;
    }

    public String getName() {
        return name;
    }

    public int getTotalSamplingTime() {
        return totalSamplingTime;
    }

    public void setNumPatientsSampled(int numPatientsSampled) {
        this.numPatientsSampled = numPatientsSampled;
    }

    public void setTotalSamplingTime(int totalSamplingTime) {
        this.totalSamplingTime = totalSamplingTime;
    }

    public double getAverageSampleTime() {
        return averageSampleTime;
    }

    public int getWorkLoad() {
        return workLoad;
    }

    public void setAverageSampleTime(double averageSampleTime) {
        this.averageSampleTime = averageSampleTime;
    }

    public void setWorkLoad(int workLoad) {
        this.workLoad = workLoad;
    }

    @Override
    public int compareTo(Nurse o) {
        return this.getAvailableAt().compareTo(o.getAvailableAt());
    }

}
