package Plans_PK;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author zosama
 */
public class EditPlanServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {
        // =====================================================================
        // ======        Get All Data From The Form  ===========================
        // =====================================================================
        
        String planid = request.getParameter("planid");
        String planname = request.getParameter("planname");
        String monthlyfee = request.getParameter("monthlyfee");
        String description = request.getParameter("description");
        String isactive = request.getParameter("isactive");
        String voice_allowance = request.getParameter("voice_allowance");
        String data_allowance = request.getParameter("data_allowance");
        String sms_allowance = request.getParameter("sms_allowance");
        String voice_free = request.getParameter("voice_free");
        String data_free = request.getParameter("data_free");
        String sms_free = request.getParameter("sms_free");
        String color = request.getParameter("color");
        
        // =====================================================================
        // ======        Set All Data to the Plan Object  ======================
        // =====================================================================
        
        int P_ID = Integer.parseInt(planid);
        PlanClass Plan = new PlanClass(P_ID);
        
        Plan.setPlanname(planname);
        
        int P_MonthlyFee = Integer.parseInt(monthlyfee);
        Plan.setMonthlyfee(P_MonthlyFee);
        
        Plan.setDescription(description);
        
        Boolean P_ISActive = Boolean.valueOf(isactive);
        Plan.setIsactive(P_ISActive);
        
        int P_voice_allowance = Integer.parseInt(voice_allowance);
        Plan.setVoice_allowance(P_voice_allowance);
               
        int P_data_allowance = Integer.parseInt(data_allowance);
        Plan.setData_allowance(P_data_allowance);

        int P_sms_allowance = Integer.parseInt(sms_allowance);
        Plan.setSms_allowance(P_sms_allowance);

        int P_voice_free = Integer.parseInt(voice_free);
        Plan.setVoice_free(P_voice_free);

        int P_data_free = Integer.parseInt(data_free);
        Plan.setData_free(P_data_free);

        int P_sms_free = Integer.parseInt(sms_free);
        Plan.setSms_free(P_sms_free);
        
        Plan.setColor(color);
        
        // =====================================================================
        // ======                  update the DB                          ======
        // =====================================================================
        String contextpath = request.getContextPath();
        Boolean UpdateStatus = Plan.EditPlanByID(Plan);
        if(UpdateStatus)
        {
            response.sendRedirect(contextpath + "/Messages/PlanUpdatedSuccess.html");
        }
        else
        {
            response.sendRedirect(contextpath + "/Messages/PlanUpdatedFaild.html");
        }
    }

}
