package com.example.studentmanagement.model;

import com.example.studentmanagement.database.DBConnection;

import java.sql.*;
import java.util.*;

public class StudentModel {

    public void addStudent(Student student) throws SQLException {
        String studentSql = "INSERT INTO students VALUES (?, ?, ?, ?, ?, ?)";
        String skillSql = "INSERT INTO student_skills (student_id, skill_name) VALUES (?, ?)";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(studentSql)) {
                ps.setInt(1, student.getStudentId());
                ps.setString(2, student.getStudentName());
                ps.setString(3, student.getEmail());
                ps.setString(4, student.getCourseName());
                ps.setDouble(5, student.getProgrammingScore());
                ps.setDouble(6, student.getMathematicsScore());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(skillSql)) {
                for (String skill : student.getSkills()) {
                    ps.setInt(1, student.getStudentId());
                    ps.setString(2, skill);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            con.commit();
        } catch (SQLException e) {
            throw e;
        }
    }

    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY student_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Student student = readStudent(rs);
                student.setSkills(getSkills(con, student.getStudentId()));
                students.add(student);
            }
        }
        return students;
    }

    public Student getStudentById(int id) throws SQLException {
        String sql = "SELECT * FROM students WHERE student_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student student = readStudent(rs);
                    student.setSkills(getSkills(con, id));
                    return student;
                }
            }
        }
        return null;
    }

    public Student searchByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM students WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student student = readStudent(rs);
                    student.setSkills(getSkills(con, student.getStudentId()));
                    return student;
                }
            }
        }
        return null;
    }

    public List<Student> searchByCourse(String course) throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE course_name = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, course);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student student = readStudent(rs);
                    student.setSkills(getSkills(con, student.getStudentId()));
                    students.add(student);
                }
            }
        }
        return students;
    }

    public void updateStudent(Student student) throws SQLException {
        String updateSql = "UPDATE students SET student_name=?, email=?, course_name=?, " +
                "programming_score=?, mathematics_score=? WHERE student_id=?";
        String deleteSkillsSql = "DELETE FROM student_skills WHERE student_id=?";
        String insertSkillSql = "INSERT INTO student_skills (student_id, skill_name) VALUES (?, ?)";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setString(1, student.getStudentName());
                ps.setString(2, student.getEmail());
                ps.setString(3, student.getCourseName());
                ps.setDouble(4, student.getProgrammingScore());
                ps.setDouble(5, student.getMathematicsScore());
                ps.setInt(6, student.getStudentId());

                int rows = ps.executeUpdate();
                if (rows == 0) {
                    con.rollback();
                    throw new SQLException("Student not found");
                }
            }

            try (PreparedStatement ps = con.prepareStatement(deleteSkillsSql)) {
                ps.setInt(1, student.getStudentId());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(insertSkillSql)) {
                for (String skill : student.getSkills()) {
                    ps.setInt(1, student.getStudentId());
                    ps.setString(2, skill);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            con.commit();
        }
    }

    public boolean deleteStudent(int id) throws SQLException {
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Student> getMeritList() throws SQLException {
        List<Student> students = getAllStudents();
        students.sort((a, b) -> Double.compare(b.getAverage(), a.getAverage()));
        return students;
    }

    public Map<List<String>, List<Integer>> getSameSkills() throws SQLException {
        Map<List<String>, List<Integer>> result = new LinkedHashMap<>();

        for (Student student : getAllStudents()) {
            List<String> skills = new ArrayList<>(student.getSkills());
            Collections.sort(skills);

            if (!skills.isEmpty()) {
                result.computeIfAbsent(skills, k -> new ArrayList<>())
                      .add(student.getStudentId());
            }
        }
        return result;
    }

    public List<Student> getProgrammingImprovement() throws SQLException {
        List<Student> result = new ArrayList<>();

        for (Student student : getAllStudents()) {
            if (student.getProgrammingScore() < 50 &&
                    student.getMathematicsScore() >= 75) {
                result.add(student);
            }
        }
        return result;
    }

    private Student readStudent(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("student_id"),
                rs.getString("student_name"),
                rs.getString("email"),
                rs.getString("course_name"),
                rs.getDouble("programming_score"),
                rs.getDouble("mathematics_score"),
                new ArrayList<>()
        );
    }

    private List<String> getSkills(Connection con, int studentId) throws SQLException {
        List<String> skills = new ArrayList<>();
        String sql = "SELECT skill_name FROM student_skills WHERE student_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    skills.add(rs.getString("skill_name"));
                }
            }
        }
        return skills;
    }
}
