package Subscribers_PK;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DeleteSubscriberServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String subscriberId = request.getParameter("id");
        SubscriberClass subscriber =  new SubscriberClass();
        if (subscriberId != null && !subscriberId.isEmpty()) {
            boolean deleted = subscriber.softDeleteSubscriber(Integer.parseInt(subscriberId));
            
            if (deleted) {
                System.out.println("Subscriber " + subscriberId + " deleted successfully");
            } else {
                System.out.println("Failed to delete subscriber " + subscriberId);
            }
        }
        
        response.sendRedirect(request.getContextPath() + "/SubscribersMainPageServlet");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/SubscribersMainPageServlet");
    }
}