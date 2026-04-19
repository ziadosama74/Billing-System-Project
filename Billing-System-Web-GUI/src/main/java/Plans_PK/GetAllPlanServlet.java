/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Plans_PK;

import Subscribers_PK.SubscriberClass;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;


public class GetAllPlanServlet extends HttpServlet {

    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {
       response.setContentType("text/html;charset=UTF-8");
       PrintWriter out = response.getWriter();
       request.getRequestDispatcher("/HTML/AddNewSubscriber.html").include(request, response);
       List<PlanClass> plans = PlanClass.GetAllPlans();
       out.print("<div class=\"plan-cards\">");
       for(PlanClass plan : plans)
       {
           LoadPlansCard(out,plan);
       }
       out.print("</div>");
       LoadRestPage(out);
    }

    // ===========================================================================================
    // ====================== Load The Table With The Subscriber =================================
    // ===========================================================================================
    private static void LoadPlansCard(PrintWriter out, PlanClass plan) 
    {
        try 
        {
           
            out.print("    <!-- Basic Plan -->");
            out.print("    <div class=\"plan-card\" data-plan=\"" + plan.getPlanname() + "\">");
            out.print("        <div class=\"plan-card-inner\">");
            out.print("            <div class=\"plan-name\" style = \"color : "+plan.getColor()+"\">"+plan.getPlanname() +"</div>");
            out.print("            <div class=\"plan-price\">EGP "+ plan.getMonthlyfee() +"<span> /month</span></div>");
            out.print("            <div class=\"plan-features\">");
            out.print("                <small><i class=\"fas fa-check\"></i>"+ plan.getVoice_allowance() +" min + "+plan.getVoice_free()+" Free</small>");
            out.print("                <small><i class=\"fas fa-check\"></i>"+ plan.getData_allowance()  / 1024.0 +" GB + "+plan.getData_free()  / 1024.0 +" Free</small>");
            out.print("               <small><i class=\"fas fa-check\"></i>"+ plan.getSms_allowance()+" SMS + "+plan.getSms_free()+" Free</small>");
            out.print("            </div>");
            out.print("        </div>");
            out.print("    </div>");
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
     private static void LoadRestPage(PrintWriter out) 
    {
        try 
        {
            out.print("<input type=\"hidden\" id=\"selectedPlan\" name=\"plan\">");
            out.print("<div class=\"error-message\" id=\"plan-error\"></div>");
            out.print("</div>");
            out.print("");
            out.print("<!-- Form Actions -->");
            out.print("<div class=\"form-actions\">");
            out.print("    <button type=\"reset\" class=\"btn btn-reset\">");
            out.print("        <i class=\"fas fa-undo-alt\"></i> Reset");
            out.print("    </button>");
            out.print("    <button type=\"submit\" class=\"btn btn-submit\">");
            out.print("        <i class=\"fas fa-save\"></i> Add Subscriber");
            out.print("    </button>");
            out.print("</div>");
            out.print("</form>");
            out.print("</div>");
            out.print("</div>");
            out.print("<script src=\"/Billing-System-Web-GUI/JS/Add_Subscriber.js\"></script>");
            out.print("</body>");
            out.print("</html>");
            
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
}
