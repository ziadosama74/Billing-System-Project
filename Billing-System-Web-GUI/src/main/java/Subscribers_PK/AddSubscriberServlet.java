package Subscribers_PK;

import Login_PK.Admin;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import Plans_PK.PlanClass;
public class AddSubscriberServlet extends HttpServlet {

    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
        String msisdn = request.getParameter("msisdn");
        String name = request.getParameter("name");
        String internationalid = request.getParameter("internationalid");
        String address = request.getParameter("address");
        String PlanID = request.getParameter("plan");
        
        SubscriberClass NewSubscriber = new SubscriberClass();
        
        NewSubscriber.setMSISDN(msisdn);
        NewSubscriber.setName(name);
        NewSubscriber.setInternational_ID(internationalid);
        NewSubscriber.setAddress(address);
        NewSubscriber.setPlanID(Integer.parseInt(PlanID));
        
        int NewSubscriberID = NewSubscriber.AddNewSubscriber(NewSubscriber);
        
        String contextPath = request.getContextPath();
        
        if(NewSubscriberID == -1)
        {
            response.sendRedirect(contextPath + "/Messages/duplicate-International-ID.html");
        }
        else if (NewSubscriberID == -2)
        {
            response.sendRedirect(contextPath + "/Messages/add-failed-duplicate--MSISDN.html");
        }
        else if (NewSubscriberID > 0)
        {
            response.sendRedirect(contextPath + "/Messages/add-subscriber-success.html?msisdn=" + msisdn + "&name=" + name + "&plan=" + PlanClass.GetPlanNameByID(Integer.parseInt(PlanID)));
        }
        else
        {
            response.sendRedirect(contextPath + "/Messages/add-subscriber-faild.html");
        }
    }
}
