import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

public class CoronaTestLane {

    private List<Patient> patients;     // all patients visiting the test lane today
    private List<Nurse> nurses;         // all nurses working at the test lane today
    private LocalTime openingTime;      // start time of sampling at the test lane today
    private LocalTime closingTime;      // latest time of possible arrivals of patients
    // hereafter, nurses will continue work until the queue is empty

    // simulation statistics for reporting
    private int maxQueueLength;             // the maximum queue length of waiting patients at any time today
    private int maxRegularWaitTime;         // the maximum wait time of regular patients today
    private int maxPriorityWaitTime;        // the maximum wait time of priority patients today
    private double averageRegularWaitTime;  // the average wait time of regular patients today
    private double averagePriorityWaitTime; // the average wait time of priority patients today
    private LocalTime workFinished;         // the time when all nurses have finished work with no more waiting patients
    private int regularPatients;            // the number of regular patients
    private int priorityPatients;           // the number of priority patients

    private Random randomizer;              // used for generation of test data and to produce reproducible simulation results

    /**
     * Instantiates a corona test line for a given day of work
     *
     * @param openingTime start time of sampling at the test lane today
     * @param closingTime latest time of possible arrivals of patients
     */
    public CoronaTestLane(LocalTime openingTime, LocalTime closingTime) {
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.workFinished = openingTime;
        this.randomizer = new Random(0);
        System.out.printf("\nCorona test lane simulation between %s and %s\n", openingTime, closingTime);
    }

    /**
     * Simulate a day at the Test Lane
     *
     * @param numNurses        the number of nurses that shall be scheduled to work in parallel
     * @param numPatients      the number of patient profiles that shall be generated to visit the Test Lane today
     * @param priorityFraction the fraction of patients that shall be given priority
     *                         and will be allowed to skip non-priority patients on the waiting queue
     * @param seed             used to initialize a randomizer to generate reproducible semi-random data
     */
    public void configure(int numNurses, int numPatients, double priorityFraction, long seed) {
        randomizer = new Random(seed);
        System.out.printf("\nConfiguring test lane with %d nurse(s) and %d patients (%.0f%% priority); seed=%d.\n",
                numNurses, numPatients, 100 * priorityFraction, seed);

        // Configure the nurses
        nurses = new ArrayList<>();
        for (int n = 0; n < numNurses; n++) {
            nurses.add(new Nurse("Nurse-" + (n + 1), openingTime, randomizer));
        }

        // Generate the full list of patients that will be arriving at the test lane (and show a few)
        patients = new ArrayList<>();
        for (int p = 0; p < numPatients; p++) {
            patients.add(new Patient(openingTime, closingTime, priorityFraction, randomizer));
        }

        // echo some patients for runtime confirmation
        if (patients.size() > 2) {
            System.out.printf("   a few patients: %s - %s - %s - ...\n", patients.get(0), patients.get(1), patients.get(2));
        }
    }

    /**
     * Simulate a day at the Test Lane and calculate the relevant statistics from this simulation
     */
    public void simulate() {
        System.out.printf("Simulating the sampling of %d patients by %d nurse(s).\n",
                patients.size(), nurses.size());

        // interleaved by nurses inviting patients from the waiting queue to have their sample taken from their nose...

        // maintain the patients queue by priority and arrival time
        Queue<Patient> waitingPatients = new PriorityQueue<>(new PatientComparator());
        for (Patient patient : patients) {
            waitingPatients.add(patient);
            waitingPatients.poll();
        }

        // reset availability of the nurses
        for (Nurse nurse : nurses) {
            nurse.setAvailableAt(openingTime);
            nurse.setNumPatientsSampled(0);
            nurse.setTotalSamplingTime(0);
        }

        // maintain a queue of nurses ordered by earliest time of availability
        Queue<Nurse> availableNurses = new PriorityQueue<>();
        availableNurses.addAll(nurses);

        // ensure patients are processed in order of arrival
        patients.sort(Comparator.comparing(Patient::getArrivedAt));

        // track the max queuelength as part of the simulation
        maxQueueLength = 0;

        // determine the first available nurse
        Nurse nextAvailableNurse = availableNurses.poll();

        // process all patients in order of arrival at the Test Lane
        for (Patient patient : patients) {
            // let nurses handle patients on the queue, if any
            // until the time of the next available nurse is later than the patient who just arrived
            while (waitingPatients.size() > 0 && nextAvailableNurse.getAvailableAt().compareTo(patient.getArrivedAt()) <= 0) {
                // handle the next patient from the queue
                Patient nextPatient = waitingPatients.poll();

                LocalTime startTime = nextAvailableNurse.getAvailableAt().isAfter(nextPatient.getArrivedAt()) ?
                        nextAvailableNurse.getAvailableAt() :
                        nextPatient.getArrivedAt();
                nextAvailableNurse.samplePatient(nextPatient, startTime);

                // reorder the current nurse into the queue of nurses as per her next availability
                // (after completing the current patient)
                availableNurses.add(nextAvailableNurse);

                // get the next available nurse for handling of the next patient
                nextAvailableNurse = availableNurses.poll();
            }

            // add the patient that just arrived to the queue before letting the nurses proceed
            waitingPatients.add(patient);

            // keep track of the maximum queue length
            maxQueueLength = Integer.max(maxQueueLength, waitingPatients.size());
        }

        // process the remaining patients on the queue, same as above
        while (waitingPatients.size() > 0) {
            Patient nextPatient = waitingPatients.poll();
            LocalTime startTime = nextAvailableNurse.getAvailableAt().isAfter(nextPatient.getArrivedAt()) ?
                    nextAvailableNurse.getAvailableAt() :
                    nextPatient.getArrivedAt();
            nextAvailableNurse.samplePatient(nextPatient, startTime);
            availableNurses.add(nextAvailableNurse);
            nextAvailableNurse = availableNurses.poll();
        }

        // all patients are underway

        // calculating average sample time for each nurse
        for (Nurse nurse : nurses) {
            double averageSampleTime = (double) nurse.getTotalSamplingTime() / nurse.getNumPatientsSampled();
            nurse.setAverageSampleTime(averageSampleTime);
        }

        // calculating work load for each nurse
        for (Nurse nurse : nurses) {
            double worked = Duration.between(getOpeningTime(), getClosingTime()).toSeconds();
            int workLoad = (int) ((nurse.getTotalSamplingTime() / worked) * 100);
            nurse.setWorkLoad(workLoad);
        }

        // calculating the time that all nurses have finished all work (including overtime)
        int totalSecondsWorked = 0;
        int idleTime = 0;
        if (waitingPatients.size() <= 0) {
            for (Nurse nurse : nurses) {
                // total seconds that nurses have been sampling
                totalSecondsWorked += nurse.getTotalSamplingTime();

                // converting the seconds worked in hours, minutes and seconds
                int hours = (totalSecondsWorked) / 3600;
                int minutes = (totalSecondsWorked % 3600) / 60;
                int seconds = totalSecondsWorked % 60;

                // adding the time worked to the opening time
                setWorkFinished(getOpeningTime().plusHours(hours).plusMinutes(minutes).plusSeconds(seconds));
            }

            for (Nurse nurse : nurses) {
                // total seconds that nurses have been idle
                idleTime += Duration.between(getWorkFinished(), nurse.getAvailableAt()).toSeconds();

                // converting the seconds worked in hours, minutes and seconds
                int hours = (totalSecondsWorked + idleTime) / 3600;
                int minutes = ((totalSecondsWorked + idleTime) % 3600) / 60;
                int seconds = (totalSecondsWorked + idleTime) % 60;

                // adding the time worked and idle time to the opening time
                setWorkFinished(getOpeningTime().plusHours(hours).plusMinutes(minutes).plusSeconds(seconds));
            }
            // calculating the overtime done by nurses
            for (Nurse nurse : nurses) {
                if (nurse.getAvailableAt().isAfter(workFinished)) {
                    workFinished = nurse.getAvailableAt();
                }
            }
        }

        // calculating average and maximum wait times for regular and priority patients (if any)
        averageRegularWaitTime = 0;
        maxRegularWaitTime = 0;

        averagePriorityWaitTime = 0;
        maxPriorityWaitTime = 0;
        for (Patient patient : patients) {
            if (!patient.isHasPriority()) {
                // counting every regular patient
                regularPatients++;

                // average wait time
                double patientWaited = Duration.between(patient.getArrivedAt(), patient.getSampledAt()).toSeconds();
                averageRegularWaitTime += patientWaited;

                // maximum waiting time
                if (patientWaited > maxRegularWaitTime) {
                    maxRegularWaitTime = (int) patientWaited;
                }
            } else {
                // counting every priority patient
                priorityPatients++;

                // average wait time
                double patientWaited = Duration.between(patient.getArrivedAt(), patient.getSampledAt()).toSeconds();
                averagePriorityWaitTime += patientWaited;

                // maximum waiting time
                if (patientWaited > maxPriorityWaitTime) {
                    maxPriorityWaitTime = (int) patientWaited;
                }
            }
        }
    }

    /**
     * Report the statistics of the simulation
     */
    public void printSimulationResults() {
        System.out.println("Simulation results per nurse:");
        System.out.println("    Name: #Patients:    Avg. sample time: Workload:");

        // printing per nurse: number of patients, average sample time, and percentage
        // of hours actually spent on taking samples
        for (Nurse nurse : nurses) {
            System.out.printf("%s\t\t\t%d\t\t\t\t  %.2f\t\t%d%% \n",
                    nurse.getName(), nurse.getNumPatientsSampled(), nurse.getAverageSampleTime(), nurse.getWorkLoad());
        }

        // printing the time all nurses had finished all sampling work
        System.out.println("Work finished at " + workFinished);

        // printing the maximum length of the queue at any time
        System.out.println("Maximum patient queue length = " + maxQueueLength);

        // reporting average and maximum wait times for regular patients
        System.out.printf("Wait times:        Average:  Maximum:\n");
        System.out.printf("Regular patients:    %.2f       %d \n",
                (averageRegularWaitTime / regularPatients), maxRegularWaitTime);

        // reporting average and maximum wait times for priority patients (if any)
        if (this.averagePriorityWaitTime > 0) {
            System.out.printf("Priority patients:    %.2f       %d \n",
                    (averagePriorityWaitTime / priorityPatients), maxPriorityWaitTime);
        }
    }

    /**
     * Report the statistics of the patients
     */
    public void printPatientStatistics() {
        System.out.println("\nPatient counts by zip area:");
        Map<String, Integer> patientCounts = patientsByZipArea();
        System.out.println(patientsByZipArea());

        System.out.println("\nZip area with highest patient percentage per complaint:");
        Map<Patient.Symptom, String> zipAreasPerSymptom =
                zipAreasWithHighestPatientPercentageBySymptom(patientCounts);
        System.out.println(zipAreasPerSymptom);
    }

    /**
     * Calculate the number of patients per zip-area code (i.e. the digits of a zipcode)
     *
     * @return a map of patient counts per zip-area code
     */
    public Map<String, Integer> patientsByZipArea() {
        // creating the result map
        Map<String, Integer> result = new TreeMap<>();

        // populating the result map
        for (Patient patient : patients) {
            result.merge(patient.getZipCode().substring(0, patient.getZipCode().length() - 2), 1, Integer::sum);
        }

        // returning the result map
        return result;
    }

    public Map<Patient.Symptom, String> zipAreasWithHighestPatientPercentageBySymptom(Map<String, Integer> patientsByZipArea) {
        Map<Patient.Symptom, String> result = new TreeMap<>();

        // looping through patients and get highest appearing symptom
        for (Patient patient : patients) {
            // looping through all symptoms
            for (int i = 0; i < patient.getSymptoms().length; i++) {
                // if symptom is true add it to the treemap
                if (patient.getSymptoms()[i]) {
                    result.put(Patient.Symptom.values()[i], patient.getZipCode().substring(0, patient.getZipCode().length() - 2));
                }
            }
        }
        return result;
    }

    public List<Patient> getPatients() {
        return patients;
    }

    public List<Nurse> getNurses() {
        return nurses;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public int getMaxQueueLength() {
        return maxQueueLength;
    }

    public int getMaxRegularWaitTime() {
        return maxRegularWaitTime;
    }

    public int getMaxPriorityWaitTime() {
        return maxPriorityWaitTime;
    }

    public double getAverageRegularWaitTime() {
        return averageRegularWaitTime;
    }

    public double getAveragePriorityWaitTime() {
        return averagePriorityWaitTime;
    }

    public LocalTime getWorkFinished() {
        return workFinished;
    }

    public void setWorkFinished(LocalTime workFinished) {
        this.workFinished = workFinished;
    }
}
