package servlet;

import dao.StudentDAO;
import model.Student;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

public class StudentServlet extends HttpServlet {

    private StudentDAO dao = new StudentDAO();
    private Gson gson = new Gson();

    // GET (Read)
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");

        String idParam = req.getParameter("id");

        if (idParam == null) {
            List<Student> students = dao.getAll();
            resp.getWriter().write(gson.toJson(students));
        } else {
            int id = Integer.parseInt(idParam);
            Student student = dao.getById(id);
            resp.getWriter().write(gson.toJson(student));
        }
    }

    // POST (Create)
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        BufferedReader reader = req.getReader();
        Student student = gson.fromJson(reader, Student.class);

        Student created = dao.create(student);

        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(created));
    }

    // PUT (Update)
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String idParam = req.getParameter("id");
        int id = Integer.parseInt(idParam);

        BufferedReader reader = req.getReader();
        Student student = gson.fromJson(reader, Student.class);

        boolean updated = dao.update(id, student);

        resp.getWriter().write(updated ? "Updated" : "Not Found");
    }

    // DELETE
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String idParam = req.getParameter("id");
        int id = Integer.parseInt(idParam);

        boolean deleted = dao.delete(id);

        resp.getWriter().write(deleted ? "Deleted" : "Not Found");
    }
}