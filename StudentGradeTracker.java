import java.util.Scanner;

/**
 * CodeAlpha Java Internship - Task 1
 * Student Grade Tracker
 *
 * Compatible with Java 21.
 */
public class StudentGradeTracker {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("================================");
        System.out.println("       STUDENT GRADE TRACKER");
        System.out.println("================================");

        int studentCount = readPositiveInt(scanner, "Enter number of students: ");

        String[] names = new String[studentCount];
        double[] averages = new double[studentCount];

        double totalClassAverage = 0.0;
        double highest = -1.0;
        double lowest = Double.MAX_VALUE;
        String highestStudent = "";
        String lowestStudent = "";

        for (int i = 0; i < studentCount; i++) {
            System.out.println("\nStudent " + (i + 1));

            System.out.print("Enter student name: ");
            String name = scanner.nextLine().trim();
            while (name.isEmpty()) {
                System.out.print("Name cannot be empty. Enter student name: ");
                name = scanner.nextLine().trim();
            }
            names[i] = name;

            int subjectCount = readPositiveInt(
                    scanner, "Enter number of subjects for " + name + ": ");

            double totalMarks = 0.0;

            for (int j = 0; j < subjectCount; j++) {
                totalMarks += readMarks(
                        scanner, "Enter marks for subject " + (j + 1) + " (0-100): ");
            }

            double average = totalMarks / subjectCount;
            averages[i] = average;
            totalClassAverage += average;

            if (average > highest) {
                highest = average;
                highestStudent = name;
            }

            if (average < lowest) {
                lowest = average;
                lowestStudent = name;
            }
        }

        double classAverage = totalClassAverage / studentCount;

        System.out.println("\n================================");
        System.out.println("          SUMMARY REPORT");
        System.out.println("================================");

        for (int i = 0; i < studentCount; i++) {
            System.out.printf("%-20s Average: %.2f%n", names[i], averages[i]);
        }

        System.out.println("--------------------------------");
        System.out.printf("Class Average   : %.2f%n", classAverage);
        System.out.printf("Highest Scorer  : %s (%.2f)%n", highestStudent, highest);
        System.out.printf("Lowest Scorer   : %s (%.2f)%n", lowestStudent, lowest);
        System.out.println("================================");

        scanner.close();
    }

    private static int readPositiveInt(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(input);
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Ask again below.
            }

            System.out.println("Please enter a valid positive whole number.");
        }
    }

    private static double readMarks(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();

            try {
                double marks = Double.parseDouble(input);
                if (marks >= 0 && marks <= 100) {
                    return marks;
                }
            } catch (NumberFormatException ignored) {
                // Ask again below.
            }

            System.out.println("Please enter marks between 0 and 100.");
        }
    }
}
