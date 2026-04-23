
package Plans_PK;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

public class OpenPlansPageServelt extends HttpServlet 
{

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {
        String contextpath = request.getContextPath();
        List<PlanClass> Plans = PlanClass.GetAllPlans();
        ObjectMapper data = new ObjectMapper();
        
        String realPath = getServletContext().getRealPath("/JSON");
        File dir = new File(realPath);
        File plans_data_file = new File(dir, "plans_data.json");
        
        data.writeValue(plans_data_file, Plans);

        response.sendRedirect(contextpath + "/HTML/plans.html");
    }
}
