import utils.SLF4J;
import utils.XMLParser;

import javax.xml.stream.XMLStreamConstants;
import java.time.Month;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PPS {

    private static Random randomizer = new Random(06112020);

    private String name;                // the name of the planning system refers to its xml source file
    private int planningYear;           // the year indicates the period of start and end dates of the projects
    private Set<Employee> employees;
    private Set<Project> projects;

    @Override
    public String toString() {
        return String.format("PPS_e%d_p%d", this.employees.size(), this.projects.size());
    }

    private PPS() {
        name = "none";
        planningYear = 2000;
        projects = new TreeSet<>();
        employees = new TreeSet<>();
    }
    private PPS(String resourceName, int year) {
        this();
        name = resourceName;
        planningYear = year;
    }

    /**
     * Reports the statistics of the project planning year
     */
    public void printPlanningStatistics() {
        System.out.printf("\nProject Statistics of '%s' in the year %d\n", name, planningYear);
        if (employees == null || projects == null || employees.size() == 0 || projects.size() == 0) {
            System.out.println("No employees or projects have been set up...");
            return;
        }

        System.out.printf("%d employees have been assigned to %d projects:\n\n",
                employees.size(), projects.size());

        System.out.printf("1. The average hourly wage of all employees is %.2f\n",
                calculateAverageHourlyWage());
        System.out.printf("2. The longest project is '%s' with %d available working days.\n",
                calculateLongestProject().toString(),
                calculateLongestProject().getNumWorkingDays());
        System.out.println("3. The follow employees have the broadest assignment in no less than "
                + calculateMostInvolvedEmployeesInAmountOfProjects() + " different projects: \n"
                + calculateMostInvolvedEmployees());
        System.out.printf("4. The total budget of committed project manpower is %d\n",
                calculateTotalManpowerBudget());
        System.out.println("5. Below is an overview of total managed budget by junior employees (hourly wage <= 26):\n"
                + calculateManagedBudgetOverview(receivesMaxJuniorWage()));
        System.out.println("6. Below is an overview of employees working at least 8 hours per day:\n" + getFulltimeEmployees());
        System.out.println("7. Below is an overview of cumulative monthly project spends:\n" + calculateCumulativeMonthlySpends());
    }

    /**
     * calculates the average hourly wage of all known employees in this system
     * @return the average wage
     */
    public double calculateAverageHourlyWage() {
        double averageWage = employees.stream().mapToDouble(Employee::getHourlyWage).sum();
        averageWage /= employees.size();
        return averageWage;
    }

    /**
     * finds the project with the highest number of available working days.
     * (if more than one project with the highest number is found, any one is returned)
     * @return the longest project
     */
    public Project calculateLongestProject() {
        Project longestProject = projects.stream().max(Comparator.comparing(Project::getNumWorkingDays)).get();
        return longestProject;
    }

    /**
     * calculates the total budget for assigned employees across all projects and employees in the system
     * based on the registration of committed hours per day per employee,
     * the number of working days in each project
     * and the hourly rate of each employee
     * @return the total manpower budget
     */
    public int calculateTotalManpowerBudget() {
        return this.employees
                .stream()
                .mapToInt(e -> e.getManagedProjects()
                        .stream()
                        .mapToInt(Project::calculateManpowerBudget).sum()
                ).sum();
    }

    /**
     * finds the employees that are assigned to the highest number of different projects
     * (if multiple employees are assigned to the same highest number of projects,
     * all these employees are returned in the set)
     * @return set of most involved employees
     */
    public Set<Employee> calculateMostInvolvedEmployees() {
        int mostInvolved = employees.stream()
                .mapToInt(employee -> employee.getAssignedProjects().size())
                // 0 is the minimum value or if stream is empty
                // get maximum value from the list
                .reduce(0, (a, b) -> Math.max(b, a));

        // filtering to find the employee with the most assigned projects
        return employees.stream()
                .filter(employee -> employee.getAssignedProjects().size() >= mostInvolved)
                // converting to set
                .collect(Collectors.toSet());
    }

    /**
     * Predicate helper which is suited for employees that earn the maximum junior wage or less.
     * @return predicate
     */
    public static Predicate<Employee> receivesMaxJuniorWage() {
        return e -> e.getHourlyWage() <= Employee.MAX_JUNIOR_WAGE;
    }

    public int calculateMostInvolvedEmployeesInAmountOfProjects() {
        return calculateMostInvolvedEmployees().stream().findFirst().get().getAssignedProjects().size();
    }

    /**
     * Calculates an overview of total managed budget per employee that complies with the filter predicate
     * The total managed budget of an employee is the sum of all man power budgets of all projects
     * that are being managed by this employee
     * @param filter the employee that manages
     * @return total managed budget per employee
     */
    public Map<Employee,Integer> calculateManagedBudgetOverview(Predicate<Employee> filter) {
        // finding the employees that comply with the filter predicate
        return employees.stream()
                .filter(filter)
                // summing up the total managed budget
                .collect(Collectors.toMap(x -> x, Employee::calculateManagedBudget));
    }

    /**
     * Calculates and overview of total monthly spends across all projects in the system
     * The monthly spend of a single project is the accumulated manpower cost of all employees assigned to the
     * project across all working days in the month.
     * @return a map of total monthly spends
     */
    public Map<Month,Integer> calculateCumulativeMonthlySpends() {
        // creating treemap to store the months and the spends
        Map<Month, Integer> monthlySpends = new TreeMap<>();

        // going through all projects
        // setting the month and the spend which is the accumulated manpower cost of all employees
        projects.forEach(project -> {
            project.getWorkingDays().stream()
                    .forEach(date -> {
                        monthlySpends.put(date.getMonth(), monthlySpends.getOrDefault(date.getMonth(), 0) + (project.calculateManpowerBudget() / project.getWorkingDays().size()));
            });
        });

        return monthlySpends;
    }

    /**
     * Returns a set containing all the employees that work at least fulltime for at least one day per week on a project.
     * @return a set of fulltime employees
     */
    public Set<Employee> getFulltimeEmployees() {
        Set<Employee> fullTimeEmp = new TreeSet<>();

        employees.forEach(employee ->
                employee.getAssignedProjects().forEach(project -> {
                    project.getCommittedHoursPerDay().forEach((key, value) -> {
                        if (value >= 8) {
                            fullTimeEmp.add(key);
                        }
                    });
            }));
        return fullTimeEmp;
    }

    public String getName() {
        return name;
    }

    /**
     * A builder helper class to compose a small PPS using method-chaining of builder methods
     */
    public static class Builder {
        PPS pps;

        public Builder() {
            pps = new PPS();
        }

        /**
         * Add another employee to the PPS being build
         * @param employee the employee that is being added
         * @return
         */
        public Builder addEmployee(Employee employee) {
            pps.employees.add(employee);
            return this;
        }

        /**
         * Add another project to the PPS
         * register the specified manager as the manager of the new
         * @param project project that is being added
         * @param manager manager assigned to the project
         * @return
         */
        public Builder addProject(Project project, Employee manager) {
            if (manager.getNumber() == 60006){
                System.out.println();
            }

            Employee employee = pps.employees.stream().filter(e -> e.equals(manager)).findFirst().orElse(manager);

            pps.projects.add(project);
            pps.employees.add(employee);
            employee.getManagedProjects().add(project);

            return this;
        }

        /**
         * Add a commitment to work hoursPerDay on the project that is identified by projectCode
         * for the employee who is identified by employeeNr
         * This commitment is added to any other commitment that the same employee already
         * has got registered on the same project,
         * @param projectCode the projectcode
         * @param employeeNr the number of the employee
         * @param hoursPerDay the hours per day worked by the specific employee on the specific project
         * @return
         */
        public Builder addCommitment(String projectCode, int employeeNr, int hoursPerDay) {
            Project project = pps.projects.stream().filter(p -> p.getCode().equals(projectCode)).findFirst().orElse(null);

            if(project != null){
                Employee employee = pps.employees.stream().filter(e -> e.getNumber() == employeeNr).findFirst().orElse(null);
                if(employee != null){
                    project.getCommittedHoursPerDay().put(employee, project.getCommittedHoursPerDay().getOrDefault(employee, 0) + hoursPerDay);
                }
            }

            return this;
        }

        /**
         * Complete the PPS being build
         *
         * @return
         */
        public PPS build() {
            return pps;
        }
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    /**
     * Loads a complete configuration from an XML file
     *
     * @param resourceName the XML file name to be found in the resources folder
     * @return
     */
    public static PPS importFromXML(String resourceName) {
        XMLParser xmlParser = new XMLParser(resourceName);

        try {
            xmlParser.nextTag();
            xmlParser.require(XMLStreamConstants.START_ELEMENT, null, "projectPlanning");
            int year = xmlParser.getIntegerAttributeValue(null, "year", 2000);
            xmlParser.nextTag();

            PPS pps = new PPS(resourceName, year);

            Project.importProjectsFromXML(xmlParser, pps.projects);
            Employee.importEmployeesFromXML(xmlParser, pps.employees, pps.projects);

            return pps;

        } catch (Exception ex) {
            SLF4J.logException("XML error in '" + resourceName + "'", ex);
        }

        return null;
    }
}
