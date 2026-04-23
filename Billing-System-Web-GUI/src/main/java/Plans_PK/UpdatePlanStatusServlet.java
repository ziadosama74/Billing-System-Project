package Plans_PK;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class UpdatePlanStatusServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {
       String planid = request.getParameter("planid");
       String status = request.getParameter("status");
       
       int ID = Integer.parseInt(planid);
       Boolean New_Sataus = Boolean.valueOf(status);
       
       PlanClass plan =  new PlanClass(ID);
       
       Boolean Function_Status = plan.update_plan_status(ID, New_Sataus);
       
       String contextpath = request.getContextPath();
       if(Function_Status)
       {
           response.sendRedirect(contextpath + "/OpenPlansPageServelt");
       }
       else
       {
           response.sendRedirect(contextpath + "/Messages/PlanStatusError.html");
       }
    }

}
