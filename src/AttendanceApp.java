import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class AttendanceApp {
    private static final Path DATA_FILE = Path.of("data", "attendance.csv");
    private static final Scanner scanner = new Scanner(System.in);
    private static final Map<String, Student> students = new LinkedHashMap<>();
    private static final Map<String, AttendanceStatus> attendance = new LinkedHashMap<>();

    public static void main(String[] args) {
        loadData();
        System.out.println("\n=== Student Attendance Manager ===");
        System.out.println("Attendance data: " + DATA_FILE.toAbsolutePath());

        boolean running = true;
        while (running) {
            printMenu();
            switch (readLine("Choose an option: ")) {
                case "1" -> addStudent();
                case "2" -> listStudents();
                case "3" -> recordAttendance();
                case "4" -> viewReport();
                case "5" -> {
                    saveData();
                    System.out.println("Data saved. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Please choose a number from 1 to 5.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n1. Add student");
        System.out.println("2. List students");
        System.out.println("3. Record attendance");
        System.out.println("4. View attendance report");
        System.out.println("5. Save and exit");
    }

    private static void addStudent() {
        String id = readLine("Student ID: ");
        String name = readLine("Student name: ");
        if (id.isBlank() || name.isBlank()) {
            System.out.println("Student ID and name are required.");
            return;
        }
        if (students.containsKey(id)) {
            System.out.println("A student with that ID already exists.");
            return;
        }
        students.put(id, new Student(id, name));
        saveData();
        System.out.println("Student added.");
    }

    private static void listStudents() {
        if (students.isEmpty()) {
            System.out.println("No students have been added yet.");
            return;
        }
        System.out.println("\nStudents:");
        students.values().forEach(student ->
                System.out.printf("%s - %s%n", student.id(), student.name()));
    }

    private static void recordAttendance() {
        if (students.isEmpty()) {
            System.out.println("Add at least one student first.");
            return;
        }

        LocalDate date = readDate("Attendance date (YYYY-MM-DD, blank for today): ");
        if (date == null) {
            date = LocalDate.now();
        }
        String dateText = date.toString();
        System.out.println("Recording attendance for " + dateText);

        for (Student student : students.values()) {
            String answer;
            do {
                answer = readLine(student.id() + " - " + student.name() + " (P=Present, A=Absent): ").toUpperCase();
            } while (!answer.equals("P") && !answer.equals("A"));
            attendance.put(key(dateText, student.id()), new AttendanceStatus(dateText, student.id(), answer.equals("P")));
        }
        saveData();
        System.out.println("Attendance saved for " + dateText + ".");
    }

    private static void viewReport() {
        if (students.isEmpty()) {
            System.out.println("No students have been added yet.");
            return;
        }

        LocalDate selectedDate = readDate("Report date (YYYY-MM-DD, blank for all dates): ");
        System.out.println("\nAttendance report");
        System.out.printf("%-12s %-24s %-10s %-10s %-10s%n", "ID", "Name", "Present", "Absent", "Rate");
        System.out.println("-".repeat(70));

        for (Student student : students.values()) {
            int present = 0;
            int absent = 0;
            for (AttendanceStatus record : attendance.values()) {
                if (record.studentId().equals(student.id())
                        && (selectedDate == null || record.date().equals(selectedDate.toString()))) {
                    if (record.present()) {
                        present++;
                    } else {
                        absent++;
                    }
                }
            }
            int total = present + absent;
            String rate = total == 0 ? "-" : String.format("%.1f%%", present * 100.0 / total);
            System.out.printf("%-12s %-24s %-10d %-10d %-10s%n", student.id(), student.name(), present, absent, rate);
        }
    }

    private static LocalDate readDate(String prompt) {
        String value = readLine(prompt);
        if (value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            System.out.println("Invalid date. Use YYYY-MM-DD.");
            return readDate(prompt);
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static String key(String date, String studentId) {
        return date + "|" + studentId;
    }

    private static void loadData() {
        if (!Files.exists(DATA_FILE)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(DATA_FILE)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",", -1);
                if (fields.length != 4) {
                    continue;
                }
                String date = fields[0];
                String studentId = fields[1];
                String name = fields[2];
                boolean present = fields[3].equalsIgnoreCase("P");
                students.putIfAbsent(studentId, new Student(studentId, name));
                attendance.put(key(date, studentId), new AttendanceStatus(date, studentId, present));
            }
        } catch (IOException exception) {
            System.out.println("Could not load saved data: " + exception.getMessage());
        }
    }

    private static void saveData() {
        try {
            Files.createDirectories(DATA_FILE.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(DATA_FILE,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                for (AttendanceStatus record : attendance.values()) {
                    Student student = students.get(record.studentId());
                    if (student != null) {
                        writer.write(String.join(",", record.date(), student.id(), student.name(), record.present() ? "P" : "A"));
                        writer.newLine();
                    }
                }
            }
        } catch (IOException exception) {
            System.out.println("Could not save data: " + exception.getMessage());
        }
    }

    private record Student(String id, String name) {
    }

    private record AttendanceStatus(String date, String studentId, boolean present) {
    }
}
