import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReferenceCalendarTest {
    Project project1, project2, project3;
    Employee employee1, employee2, employee3;
    private PPS pps;

    @BeforeEach
    void setup() {
        project1 = new Project("P1001", "TestProject-1", LocalDate.of(2019,2,1), LocalDate.of(2019,2,2));
        project2 = new Project("P2002", "TestProject-2", LocalDate.of(2019,4,1), LocalDate.of(2019,5,31));
        project3 = new Project("P3003", "TestProject-3", LocalDate.of(2019,3,15), LocalDate.of(2019,4,15));
        employee1 = new Employee(60006, 20);
        employee2 = new Employee(77007, 25);
        employee3 = new Employee(88808, 30);
        pps = new PPS.Builder()
                .addEmployee(employee1)
                .addEmployee(employee3)
                .addProject(project1, employee1)
                .addProject(project2, new Employee(60006))
                .addProject(project3, employee2)
                .addCommitment("P1001", 60006, 4)
                .addCommitment("P1001", 77007, 3)
                .addCommitment("P1001", 88808, 2)
                .addCommitment("P2002", 88808, 3)
                .addCommitment("P2002", 88808, 1)
                .build();
    }

    @Test
    void aSingleWorkingDayShouldBeReportedAsSuch() {
        // Should be changed to "Only 1 working day"?
//        assertEquals("[2019-02-01]", project1.getWorkingDays().toString());
        assertEquals(1, project1.getNumWorkingDays());
    }

    @Test
    void methodsShouldReportSameNumberOfWorkingDays() {
        // Did not understand this one
    }

    @Test
    void methodsShouldReportTheCorrectNumberOfWorkingDays() {
        assertEquals(1, project1.getNumWorkingDays());
        assertEquals(45, project2.getNumWorkingDays());
        assertEquals(22, project3.getNumWorkingDays());
    }
}
