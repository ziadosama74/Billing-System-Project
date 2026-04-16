package Main_Process;

import CDR_PK.CDR_File;
import CDR_PK.CDR_Raw_Data;
import Network.CDR_Parsing_Network;
import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CDR_Parsing_Files_Process {
    
    static CDR_Parsing_Network network = new CDR_Parsing_Network();
    
    public static void main(String[] args) throws InterruptedException 
    {
        network.StartServer();

        String home = System.getProperty("user.home");
        String CDR_Loader_Path = home + "/Billing-System-Project/CDR_Loader_Files/";
        File CDR_Loader_Files_Directory = new File(CDR_Loader_Path);

        if (!CDR_Loader_Files_Directory.exists() || !CDR_Loader_Files_Directory.isDirectory()) {
            System.out.println("CDR_Loader directory is not found !!!");
            System.exit(0);
        }

        System.out.println("══════════════════════════════════════════════════════════════════════════════");
        System.out.println("📂 CDR Parsing Files Process is Running ⚙️");
        System.out.println("══════════════════════════════════════════════════════════════════════════════");
        int FN = 0;
        while (true) 
        {

            File[] CDR_Files_CSV = CDR_Loader_Files_Directory.listFiles((dir, name) -> name.endsWith(".csv"));

            if (CDR_Files_CSV == null || CDR_Files_CSV.length == 0) {
                System.out.println("[📂⚙️ PARSING] :  No Files Yet ⚠️");
                TimeUnit.SECONDS.sleep(1);
                continue;
            }
            
            for (File cdr_csv_file : CDR_Files_CSV) 
            {

                FN++;
                CDR_File CDR_File_Info = new CDR_File(cdr_csv_file.getName());

                int FileID = CDR_File_Info.CDR_File_Insert_Recived_File(CDR_File_Info.getCDR_File_Name());

                System.out.println("══════════════════════════════════════════════════════════════════════════════");
                System.out.println("[📂⚙️ PARSING] : 📂 Loading CDR File → " + FN + " 📊⚡ Actual File ID : ("+ FileID +")");
                System.out.println("══════════════════════════════════════════════════════════════════════════════");

                List<CDR_Raw_Data> records = CDR_Raw_Data.ReadCSV(cdr_csv_file, FileID);

                int RN = 0;

                for (CDR_Raw_Data record : records) 
                {
                    RN++;
                    record.InsertCDRRecordFiled(record);
                    System.out.println("[📂⚙️ PARSING] : ⚡ Processing CDR Record (" + RN + ") 📄");
                }

                System.out.println("══════════════════════════════════════════════════════════════════════════════");
                System.out.println("[📂⚙️ PARSING] : ✅ Finished Loading File (" + FN + ") 📂");
                System.out.println("══════════════════════════════════════════════════════════════════════════════");
                
                network.SendFlagToRating(FileID);
                network.MoveToBackup(cdr_csv_file);
                network.WaitForRatingResponse();
            }
        }
    }
}
