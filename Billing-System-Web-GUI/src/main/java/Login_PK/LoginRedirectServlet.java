package Login_PK;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginRedirectServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {
        String contextPath = request.getContextPath();

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        System.out.println("USERNAME: " + username);
        System.out.println("PASSWORD: " + password);

        Admin loggedUser = Admin.login(username, password);

        if (loggedUser != null)
        {
            response.sendRedirect(contextPath + "/HTML/dashboard.html");
        }
        else
        {
            response.sendRedirect(contextPath + "/Messages/WrongLogin.html");
        }
    }
}