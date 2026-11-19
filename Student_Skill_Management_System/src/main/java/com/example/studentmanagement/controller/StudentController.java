package com.example.studentmanagement.controller;

import com.example.studentmanagement.model.Student;
import com.example.studentmanagement.model.StudentModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentModel model = new StudentModel();

    @PostMapping
    public ResponseEntity<?> addStudent(@RequestBody Student student) {
        String error = validateStudent(student);
        if (error != null) {
            return ResponseEntity.badRequest().body(error);
        }

        try {
            model.addStudent(student);
            return ResponseEntity.status(HttpStatus.CREATED).body("Student added successfully");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                return ResponseEntity.badRequest().body("Student ID or email already exists");
            }
            return ResponseEntity.internalServerError().body("Database error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllStudents() {
        try {
            return ResponseEntity.ok(model.getAllStudents());
        } catch (SQLException e) {
            return databaseError(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentById(@PathVariable int id) {
        try {
            Student student = model.getStudentById(id);
            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found");
            }
            return ResponseEntity.ok(student);
        } catch (SQLException e) {
            return databaseError(e);
        }
    }

    @GetMapping("/search/email")
    public ResponseEntity<?> searchByEmail(@RequestParam String email) {
        try {
            Student student = model.searchByEmail(email);
            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found");
            }
            return ResponseEntity.ok(student);
        } catch (SQLException e) {
            return databaseError(e);
        }
    }

    @GetMapping("/search/course")
    public ResponseEntity<?> searchByCourse(@RequestParam String course) {
        try {
            return ResponseEntity.ok(model.searchByCourse(course));
        } catch (SQLException e) {
            return databaseError(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable int id, @RequestBody Student student) {
        if (id != student.getStudentId()) {
            return ResponseEntity.badRequest().body("Path ID and student ID must match");
        }

        String error = validateStudent(student);
        if (error != null) {
            return ResponseEntity.badRequest().body(error);
        }

        try {
            model.updateStudent(student);
            return ResponseEntity.ok("Student updated successfully");
        } catch (SQLException e) {
            if ("Student not found".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return databaseError(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable int id) {
        try {
            if (model.deleteStudent(id)) {
                return ResponseEntity.ok("Student deleted successfully");
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found");
        } catch (SQLException e) {
            return databaseError(e);
        }
    }

    @GetMapping("/merit-list")
    public ResponseEntity<?> getMeritList() {
        try {
            return ResponseEntity.ok(model.getMeritList());
        } catch (SQLException e) {
            return databaseError(e);
        }
    }

    @GetMapping("/same-skills")
    public ResponseEntity<?> getSameSkills() {
        try {
            return ResponseEntity.ok(model.getSameSkills());
        } catch (SQLException e) {
            return databaseError(e);
        }
    }

    @GetMapping("/programming-improvement")
    public ResponseEntity<?> getProgrammingImprovement() {
        try {
            return ResponseEntity.ok(model.getProgrammingImprovement());
        } catch (SQLException e) {
            return databaseError(e);
        }
    }

    private String validateStudent(Student student) {
        if (student == null) {
            return "Student data is required";
        }
        if (student.getStudentId() <= 0) {
            return "Student ID must be positive";
        }
        if (student.getStudentName() == null || student.getStudentName().isBlank()) {
            return "Student name is required";
        }
        if (student.getEmail() == null || student.getEmail().isBlank() ||
                !student.getEmail().contains("@")) {
            return "Enter a valid email";
        }
        if (student.getCourseName() == null || student.getCourseName().isBlank()) {
            return "Course name is required";
        }
        if (student.getProgrammingScore() < 0 || student.getProgrammingScore() > 100 ||
                student.getMathematicsScore() < 0 || student.getMathematicsScore() > 100) {
            return "Scores must be between 0 and 100";
        }
        return null;
    }

    private ResponseEntity<?> databaseError(SQLException e) {
        return ResponseEntity.internalServerError().body("Database error: " + e.getMessage());
    }
}
