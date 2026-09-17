# Student Attendance Manager

A basic Java console application for managing student attendance.

## Features

- Add and list students
- Record present/absent status for each student and date
- View attendance reports for one date or all saved dates
- Automatically save data to `data/attendance.csv`

## Run

Requires Java 16 or newer.

```powershell
javac -d out src\AttendanceApp.java
java -cp out AttendanceApp
```

The CSV file is created automatically when the first student or attendance record is saved. Keep the file with the project so attendance remains available between runs.
