package Subscribers_PK;

import Plans_PK.PlanClass;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditSubscriberServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String subscriberId = request.getParameter("id");

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        if (subscriberId != null && !subscriberId.isEmpty()) {

            SubscriberClass subscriber = new SubscriberClass();
            subscriber = subscriber.GetSubscriberByID(Integer.parseInt(subscriberId));

            Map<String, Object> data = new HashMap<>();
            data.put("subscriberid", subscriber.getID());
            data.put("msisdn", subscriber.getMSISDN());
            data.put("name", subscriber.getName());
            data.put("internationalid", subscriber.getInternational_ID());
            data.put("address", subscriber.getAddress());
            data.put("isactive", subscriber.isStatus());
            data.put("planname", subscriber.getPlan());
            data.put("planid", subscriber.getPlanID());

            ObjectMapper mapper = new ObjectMapper();
            String home = System.getProperty("user.home");
            
            String jsonPath = home + "/Billing-System-Project/Billing-System-Web-GUI/src/main/webapp/JSON/";
            
            
            File jsonDir = new File(jsonPath);
            if (!jsonDir.exists()) {
                jsonDir.mkdirs();
            }

           
            File subscriberFile = new File(jsonPath + "subscriber_data.json");
            mapper.writeValue(subscriberFile, data);
            
          
            if (!subscriberFile.exists() || subscriberFile.length() == 0) {
                throw new IOException("Failed to write subscriber_data.json");
            }

            
            File plansFile = new File(jsonPath + "plans_data.json");
            List<PlanClass> plans = PlanClass.GetAllPlans();
            mapper.writeValue(plansFile, plans);
            
            
            if (!plansFile.exists() || plansFile.length() == 0) {
                throw new IOException("Failed to write plans_data.json");
            }

          
            subscriberFile.setLastModified(System.currentTimeMillis());
            plansFile.setLastModified(System.currentTimeMillis());
            
          
            try {
                Thread.sleep(500); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
           
            if (subscriberFile.length() > 0 && plansFile.length() > 0) {
                System.out.println("Files written successfully - Subscriber: " + subscriberFile.length() + " bytes, Plans: " + plansFile.length() + " bytes");
            } else {
                System.err.println("Warning: Files may not have been written completely");
            }
        }

      
        String redirectUrl = request.getContextPath() + "/HTML/EditSubscriber.html?id=" + subscriberId + "&t=" + System.currentTimeMillis();
        response.sendRedirect(redirectUrl);
    }
}