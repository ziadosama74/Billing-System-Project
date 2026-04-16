package Network;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class CDR_Parsing_Network {

    private Socket clientSocket = null;

    // ================= SERVER =================
    public void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(5000)) 
            {
                System.out.println("[📂⚙️ PARSING] : 🟢 Parsing Started ... Waiting For Rating Process To Connect 🔄");
                clientSocket = serverSocket.accept();
                System.out.println("[⚙️💰 RATING ] : Rating Process Connected Successfully ✅");
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        }).start();
    }

    // ================= SEND FLAG =================
    public void sendFlagToRating(int FileID) {
        try 
        {
            if (clientSocket != null && !clientSocket.isClosed()) 
            {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println("START_RATING:"+FileID);
                System.out.println("[📂⚙️ PARSING] : Flag sent to Rating Process ✔️📤");
            } 
            else 
            {
                System.out.println("[⚙️💰 RATING ] : Rating Process Not Connected Yet ⏳⚠️❌");
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    // ================= Wating For Response =================
    public void waitForRatingResponse() 
    {
        try 
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String response = in.readLine();
            if ("RATING_DONE".equals(response)) 
            {
                System.out.println("[⚙️💰 RATING ] : Rating Process Finished ✅");
            } 
            else
            {
                System.out.println("[SYSTEM] : Unexpected Response ⚠️ : " + response);
            }
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    // ================= MOVE FILE =================
    public static void moveToBackup(File file) 
    {
        try 
        {
            String backupDir = System.getProperty("user.home") + "/Billing-System-Project/CDR_Backup_Files";
            File backupFolder = new File(backupDir);
            if (!backupFolder.exists()) 
            {
                backupFolder.mkdirs();
            }
            Path sourcePath = file.toPath();
            Path targetPath = Paths.get(backupDir, file.getName());
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[📂⚙️ PARSING] : CDR File Moved To Backup Successfully 📦✅");
        } 
        catch (Exception e) 
        {
            System.err.println("Error moving file: " + e.getMessage());
        }
    }
}
