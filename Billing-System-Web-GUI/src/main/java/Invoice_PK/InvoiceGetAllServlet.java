package Invoice_PK;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class InvoiceGetAllServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            List<InvoiceClass> invoices = InvoiceClass.Invoice_Get_All_Invoices();
            
            System.out.println("Number of invoices retrieved: " + (invoices != null ? invoices.size() : 0));
            
            if (invoices == null || invoices.isEmpty()) {
                invoices = new java.util.ArrayList<>();
            }
            
            String webappPath = getServletContext().getRealPath("/");
  
            File jsonDir = new File(webappPath, "JSON");
            if (!jsonDir.exists()) {
                boolean created = jsonDir.mkdirs();
                System.out.println("Created JSON directory: " + created + " at " + jsonDir.getAbsolutePath());
            }
            
            File file = new File(jsonDir, "Invoices_data.json");

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(file, invoices);
            
            if (file.exists() && file.length() > 0) {
                System.out.println("✅ File written successfully. Size: " + file.length() + " bytes");
                
                // Optional: Print first few characters of JSON to verify
                try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
                    String firstLine = br.readLine();
                    System.out.println("First line of JSON: " + (firstLine != null ? firstLine.substring(0, Math.min(100, firstLine.length())) : "empty"));
                }
            } else {
                System.out.println("❌ ERROR: File write failed!");
            }
            
            String sourcePath = System.getProperty("user.home") + 
                    "/Billing-System-Project/Billing-System-Web-GUI/src/main/webapp/JSON/";
            File sourceDir = new File(sourcePath);
            if (!sourceDir.exists()) {
                sourceDir.mkdirs();
            }
            File sourceFile = new File(sourceDir, "Invoices_data.json");
            mapper.writeValue(sourceFile, invoices);
            System.out.println("Also backed up to: " + sourceFile.getAbsolutePath());
         
            response.sendRedirect(request.getContextPath() + "/HTML/bills.html");
            
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("Error: " + e.getMessage());
        }
    }
}