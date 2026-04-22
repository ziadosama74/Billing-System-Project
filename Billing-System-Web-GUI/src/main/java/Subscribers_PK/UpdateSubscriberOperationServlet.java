package Subscribers_PK;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author zosama
 */
public class UpdateSubscriberOperationServlet extends HttpServlet 
{

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
        String contextPath = request.getContextPath();
        
        String ID = request.getParameter("id");
        String msisdn = request.getParameter("msisdn");
        String name = request.getParameter("name");
        String internationalid = request.getParameter("internationalid");
        String address = request.getParameter("address");
        String selectedPlan = request.getParameter("plan");
        String status = request.getParameter("status");
        
        int subscriberr_ID = Integer.parseInt(ID);
        
        SubscriberClass subscriber = new SubscriberClass();
        subscriber = subscriber.GetSubscriberByID(subscriberr_ID);
        
        int current_plan_ID = subscriber.getPlanID();
        int updated_plan_ID = Integer.parseInt(selectedPlan);
        
        subscriber.setMSISDN(msisdn);
        subscriber.setName(name);
        subscriber.setInternational_ID(internationalid);
        subscriber.setAddress(address);
        int New_Plan_ID = Integer.parseInt(selectedPlan);
        subscriber.setPlanID(New_Plan_ID);
        Boolean New_Status = Boolean.parseBoolean(status);
        subscriber.setStatus(New_Status);
        
        if(current_plan_ID == updated_plan_ID)
        {
            Boolean Updated_Status = subscriber.UpdateSubscriberWithoutPlan(subscriber);
            if(Updated_Status)
            {
                response.sendRedirect(contextPath + "/Messages/SubscriberUpdatedSuccess.html");
            }
            else
            {
                response.sendRedirect(contextPath + "/Messages/SubscrioberUpdatedFailed.html");
            }
        }
        else if (current_plan_ID != updated_plan_ID)
        {
            Boolean Updated_Status = subscriber.UpdateSubscriberWithPlanProrated(subscriber);
            if(Updated_Status)
            {
                response.sendRedirect(contextPath + "/Messages/SubscriberUpdatedSuccess.html");
            }
            else
            {
                response.sendRedirect(contextPath + "/Messages/SubscrioberUpdatedFailed.html");
            }
        }
        else
        {
            response.sendRedirect(contextPath + "/Messages/SubscrioberUpdatedFailed.html");
        }
    }
}
