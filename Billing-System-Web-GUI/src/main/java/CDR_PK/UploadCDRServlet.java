package CDR_PK;

import java.io.*;
import java.nio.file.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;

@MultipartConfig
public class UploadCDRServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");

        try {
            // 1. Get file part
            Part filePart = request.getPart("cdrFile");

            if (filePart == null || filePart.getSize() == 0) 
            {
                System.out.println("No file received");
                response.getWriter().write("No file uploaded");
                return;
            }

            // 2. Get safe file name
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

            System.out.println("Received file: " + fileName);
            System.out.println("Size: " + filePart.getSize() + " bytes");

            // 3. Build upload directory
            String home = System.getProperty("user.home");
            String uploadDir = home + "/Billing-System-Project/CDR_Loader_Files";

            File dir = new File(uploadDir);

            if (!dir.exists()) 
            {
                boolean created = dir.mkdirs();
                if (!created) {
                    System.out.println("Failed to create directory");
                    response.getWriter().write("Server error: cannot create folder");
                    return;
                }
            }

            // 4. Destination file
            File file = new File(dir, fileName);

            // 5. Save file
            try (InputStream input = filePart.getInputStream()) {
                Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // 6. Verify save
            if (file.exists()) 
            {
                response.sendRedirect(request.getContextPath() + "/Messages/upload_done.html");
            } 
            else 
            {
                 response.sendRedirect(request.getContextPath() + "/Messages/upload_Failed.html");
            }

        } catch (Exception e) {
            System.out.println("Error during upload:");
            e.printStackTrace();

            response.getWriter().write("Server error: " + e.getMessage());
        }
    }
}
