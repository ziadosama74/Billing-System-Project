package Subscribers_PK;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

public class SubscribersMainPageServlet extends HttpServlet {
    // ===========================================================================================
    // ======================         Do Get Function            =================================
    // ===========================================================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        request.getRequestDispatcher("/HTML/subscribers.html").include(request, response);
        String MSISDN = request.getParameter("MSISDN");
        if (MSISDN != null && !MSISDN.trim().isEmpty())
        {
            
            SubscriberClass subscriberObj = new SubscriberClass();
            SubscriberClass subscriber = subscriberObj.GetSubscriberByMSISDN(MSISDN);
            if(subscriber != null)
            {
                LoadSubscriberTable(out, subscriber);
            }
            else
            {
                out.print("<tr><td colspan='8' style='text-align: center; padding: 40px;'>");
                out.print("<h3>No subscriber found with MSISDN: " + MSISDN + "</h3>");
                out.print("</td></tr>");
            }
        }
        else
        {
            List<SubscriberClass> subscribers = SubscriberClass.GetAllSubscriber();
            for (SubscriberClass subscriber : subscribers) 
            {
                LoadSubscriberTable(out, subscriber);
            }
        }
        out.print("</tbody>");
        out.print("</tfoot>");
        out.print("</div>");
        out.print("</div>");
        out.print("</body>");
        out.print("<script src=\"https://cdn.jsdelivr.net/npm/sweetalert2@11\"></script>");
        out.print("<script src=\"/Billing-System-Web-GUI/JS/subscribers.js\"></script>");
        out.print("</html>");
    }
    // ===========================================================================================
    // ====================== Load The Table With The Subscriber =================================
    // ===========================================================================================
    private static void LoadSubscriberTable(PrintWriter out, SubscriberClass subscriber) 
    {
        try 
        {
            out.print("<tr>");
            out.print("<td>");
            out.print(subscriber.getID());
            out.print("</td>");
            out.print("<td class=\"user-cell\">");
            out.print("<div class=\"avatar\">");
            out.print(subscriber.getName().charAt(0));
            out.print("</div>");
            out.print(" <span>");
            out.print(subscriber.getName());
            out.print("</span>");
            out.print("</td>");
            out.print("<td>");
            out.print(subscriber.getMSISDN());
            out.print("</td>");
            out.print("<td>");
            out.print(subscriber.getAddress());
            out.print("</td>");
            out.print("<td>");
            out.print(subscriber.getInternational_ID());
            out.print("</td>");
            String planClass = getPlanClass(subscriber.getPlan().toLowerCase());
            out.print("<td><span class=\"plan " + planClass + "\">" + subscriber.getPlan() + "</span></td>");
            out.print("<td><span class=\"status " + (subscriber.isStatus() ? "active" : "inactive") + "\">" + (subscriber.isStatus() ? "Active" : "Inactive") + "</span></td>");
            out.print("<td class=\"actions\">");
            out.print("<form action=\"/Billing-System-Web-GUI/EditSubscriberServlet\"  method=\"Get\">");
            out.print("<input type=\"hidden\" name=\"id\" value=\"" + subscriber.getID() + "\">");
            out.print("<button class=\"icon-btn edit\"><i class=\"fa-solid fa-pen\"></i></button>");
            out.print("</form>");
            
            // Note: onsubmit calls confirmDelete and returns false, so form doesn't submit directly
            out.print("<form action=\"/Billing-System-Web-GUI/DeleteSubscriberServlet\" method=\"post\" onsubmit=\"return confirmDelete(" + subscriber.getID() + ", '" + subscriber.getName().replace("'", "\\'") + "', '" + subscriber.getMSISDN() + "');\">");
            out.print("<input type=\"hidden\" name=\"id\" value=\"" + subscriber.getID() + "\">");
            out.print("<button type=\"submit\" class=\"icon-btn delete\"><i class=\"fa-solid fa-trash\"></i></button>");
            out.print("</form>");
            
            out.print("</td>");
            out.print("</tr>");
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    // ===========================================================================================
    // ======================               Classfy Bundles      =================================
    // ===========================================================================================
    private static String getPlanClass(String planName) 
    {
        switch (planName.toLowerCase()) 
        {
            case "basic":
                return "basic";
            case "standard":
                return "standard";
            case "premium":
                return "premium";
            case "business":
                return "business";
            default:
                return "basic";
        }
    }
}