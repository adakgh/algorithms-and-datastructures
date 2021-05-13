import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReferenceOutputTest {
    Project project1, project2, project3;
    Employee employee1, employee2, employee3;
    private PPS pps;

    @BeforeEach
    void setup() {
        project1 = new Project("P1001", "TestProject-1", LocalDate.of(2019,2,1), LocalDate.of(2019,4,30));
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
                .addCommitment("P2002", 88808, 9)
                .addCommitment("P2002", 88808, 9)

                .addCommitment("P3003", 88808, 11)
                .addCommitment("P3003", 77007, 9)
                .addCommitment("P3003", 60006, 10)
                .build();
    }

    @Test
    void checkHourlyWageLarge(){
        assertEquals(20, employee1.getHourlyWage());
    }

    @Test
    void checkHourlyWageSmall(){
        assertEquals(30, employee3.getHourlyWage());
    }

    @Test
    void checkJuniorWageLarge(){
        assertTrue(employee2.getHourlyWage() <= employee2.MAX_JUNIOR_WAGE);
    }

    @Test
    void checkJuniorWageSmall(){
        assertTrue(employee1.getHourlyWage() <= employee1.MAX_JUNIOR_WAGE);
    }

    @Test
    void checkLongestProject(){
        assertEquals("TestProject-1(P1001)", String.valueOf(pps.calculateLongestProject()));
    }

    @Test
    void checkMonthlySpends(){
        // ?
        assertEquals("{FEBRUARY=215, MARCH=0, APRIL=0, MAY=120}", String.valueOf(pps.calculateCumulativeMonthlySpends()));
    }

    @Test
    void checkMostInvolvedEmployees(){
        assertEquals("[Michael K. MORALES(60006), William O. PORTER(88808), Linda Z. MARTIN(77007)]", String.valueOf(pps.calculateMostInvolvedEmployees()));
    }

    @Test
    void checkTotalManpowerBudget(){
        assertEquals(project1.calculateManpowerBudget() + project2.calculateManpowerBudget() + project2.calculateManpowerBudget(), pps.calculateTotalManpowerBudget());
    }

    @Test
    void checkTotalOvertimeLargeProject(){
        // expected overtime done = 10+11+9 (hours worked per day for the 3 employees) * 22 days (num of working days for project 3)
        // the overtime difference = 10+11+9 * 22 days - 8 hours * 22 days (minus the normal 8 hours for the num of working days)
        assertEquals((10+11+9) * project3.getNumWorkingDays() - (8 * project3.getNumWorkingDays()), (project3.getCommittedHoursPerDay().values().stream().reduce(0, Integer::sum) * project3.getNumWorkingDays()) - (8 * project3.getNumWorkingDays()));
    }

    @Test
    void checkTotalOvertimeSmallProject(){
        assertEquals((9+9) * project2.getNumWorkingDays() - (8 * project2.getNumWorkingDays()), (project2.getCommittedHoursPerDay().values().stream().reduce(0, Integer::sum) * project2.getNumWorkingDays()) - (8 * project2.getNumWorkingDays()));
    }
}
