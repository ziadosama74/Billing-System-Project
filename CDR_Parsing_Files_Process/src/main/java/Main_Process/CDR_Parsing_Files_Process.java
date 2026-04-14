package Main_Process;

import CDR_PK.CDR_File;
import CDR_PK.CDR_Raw_Data;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;

public class CDR_Parsing_Files_Process {

    static Socket clientSocket = null;

    public static void main(String[] args) throws InterruptedException {

        // start server once
        startServer();

        String home = System.getProperty("user.home");
        String CDR_Loader_Path = home + "/Billing-System-Project/CDR_Loader_Files/";
        File CDR_Loader_Files_Directory = new File(CDR_Loader_Path);

        if (!CDR_Loader_Files_Directory.exists() || !CDR_Loader_Files_Directory.isDirectory()) {
            System.out.println("CDR_Loader directory is not found !!!");
            System.exit(0);
        }

        System.out.println("====================================");
        System.out.println("CDR Parsing Files Process Is Working");
        System.out.println("====================================");

        while (true) {

            File[] CDR_Files_CSV = CDR_Loader_Files_Directory.listFiles((dir, name) -> name.endsWith(".csv"));

            if (CDR_Files_CSV == null || CDR_Files_CSV.length == 0) {
                System.out.println("No Files Yet !!");
                TimeUnit.SECONDS.sleep(1);
                continue;
            }

            int FN = 0;

            for (File cdr_csv_file : CDR_Files_CSV) 
            {

                FN++;
                CDR_File CDR_File_Info = new CDR_File(cdr_csv_file.getName());

                int FileID = CDR_File_Info.CDR_File_Insert_Recived_File(CDR_File_Info.getCDR_File_Name());

                System.out.println("=======================================");
                System.out.println("Load CDR Information File (" + FN + ")");
                System.out.println("=======================================");

                List<CDR_Raw_Data> records = CDR_Raw_Data.readCSV(cdr_csv_file, FileID);

                int RN = 0;

                for (CDR_Raw_Data record : records) 
                {
                    RN++;
                    record.insertCDRRecordFiled(record);
                    System.out.println("Load CDR Record (" + RN + ")");
                }

                System.out.println("=======================================");
                System.out.println("Finished Loading File (" + FN + ")");
                System.out.println("=======================================\n");

                moveToBackup(cdr_csv_file);
            }

            // send signal AFTER processing
            sendFlagToRating(FN);

            TimeUnit.SECONDS.sleep(5);
        }
    }

    // ================= SERVER =================
    public static void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(5000)) {

                System.out.println("Server started... Waiting for client...");

                clientSocket = serverSocket.accept();
                System.out.println("Client connected!");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ================= SEND FLAG =================
    public static void sendFlagToRating(int fileCount) {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {

                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println("START_RATING|files=" + fileCount);

                System.out.println("Flag sent to Rating Process");

            } else {
                System.out.println("Client not connected yet");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= MOVE FILE =================
    public static void moveToBackup(File file) {
        try {
            String backupDir = System.getProperty("user.home")
                    + "/Billing-System-Project/CDR_Backup_Files";

            File backupFolder = new File(backupDir);
            if (!backupFolder.exists()) {
                backupFolder.mkdirs();
            }

            Path sourcePath = file.toPath();
            Path targetPath = Paths.get(backupDir, file.getName());

            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Moved file to backup: " + targetPath);

        } catch (Exception e) {
            System.err.println("Error moving file: " + e.getMessage());
        }
    }
}