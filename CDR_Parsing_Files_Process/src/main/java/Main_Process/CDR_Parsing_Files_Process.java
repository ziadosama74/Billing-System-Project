/*============================================================================*/
 /*======================  CDRs Parsing Files   ===============================*/
 /*============================================================================*/
package Main_Process;

import CDR_PK.CDR_File;
import CDR_PK.CDR_Raw_Data;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.util.Arrays;

public class CDR_Parsing_Files_Process {

    public static void main(String[] args) throws InterruptedException {
        // get the home dirctory of the system
        String home = System.getProperty("user.home");

        // get the full path of CDR_Loader directory
        String CDR_Loader_Path = home + "/Billing-System-Project/CDR_Loader_Files/";

        // get the CDR_Loader_Files directory
        File CDR_Loader_Files_Directory = new File(CDR_Loader_Path);

        // check the existing of CDR_Loader_Files directory
        if (!CDR_Loader_Files_Directory.exists() || !CDR_Loader_Files_Directory.isDirectory()) {
            System.out.println("CDR_Loader directory is not found !!!");
            System.exit(0);
        }
        System.out.println("====================================");
        System.out.println("CDR Parsing Files Process Is Working");
        System.out.println("====================================");

        // get the new files continuously
        while (true) {
            // read all the files in the from the CDR_Loader_Files directory
            File[] CDR_Files_CSV = CDR_Loader_Files_Directory.listFiles((dir, name) -> name.endsWith(".csv"));

            // check the existing csv files in CDR_Loader_Files directory
            if (CDR_Files_CSV == null || CDR_Files_CSV.length == 0) 
            {
                System.out.println(" No Files Yet !!");
                TimeUnit.SECONDS.sleep(1);
            } 
            else 
            {
                int FN = 0 ;
                for (File cdr_csv_file : CDR_Files_CSV) 
                {
                    FN++;
                    CDR_File CDR_File_Info = new CDR_File(cdr_csv_file.getName());

                    /// inset in DB CDR_File_Info_Obj & get the ID of the inserted file
                    int FileID = CDR_File_Info.CDR_File_Insert_Recived_File(CDR_File_Info.getCDR_File_Name());
                    System.out.println("=======================================");
                    System.out.println("Load CDR Information File ("+FN+")");
                    System.out.println("=======================================");
                    List<CDR_Raw_Data> records = CDR_Raw_Data.readCSV(cdr_csv_file, FileID);
                    int RN = 0 ;
                    for (CDR_Raw_Data record : records) 
                    {
                        RN++;
                        record.insertCDRRecordFiled(record);
                        System.out.println("Load CDR Record (" + RN + ")");
                    }
                    System.out.println("=======================================");
                    System.out.println("Finishing Load CDR File ("+FN+") into DB");
                    System.out.println("=======================================\n");
                    moveToBackup(cdr_csv_file);
                    TimeUnit.SECONDS.sleep(1);
                }
                CDR_Files_CSV = new File[0];
            }
        }
    }
    //==========================================================================
    //===============    Move The old CDR to the Backup directory   ============
    //==========================================================================
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
            System.out.println("Moved file to backup: " + targetPath);

        } 
        catch (Exception e) 
        {
            System.err.println("Error moving file: " + e.getMessage());
        }
    }
}
