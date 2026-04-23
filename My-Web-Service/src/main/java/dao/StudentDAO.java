package dao;

import model.Student;
import java.util.*;

public class StudentDAO {
    private static List<Student> students = new ArrayList<>();
    private static int currentId = 1;

    public List<Student> getAll() {
        return students;
    }

    public Student getById(int id) {
        return students.stream()
                .filter(s -> s.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public Student create(Student student) {
        student.setId(currentId++);
        students.add(student);
        return student;
    }

    public boolean update(int id, Student updated) {
        Student s = getById(id);
        if (s != null) {
            s.setName(updated.getName());
            s.setEmail(updated.getEmail());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        return students.removeIf(s -> s.getId() == id);
    }
}