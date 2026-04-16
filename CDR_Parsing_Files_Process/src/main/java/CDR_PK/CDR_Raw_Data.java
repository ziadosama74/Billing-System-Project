package CDR_PK;

import DB.BillingDB;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;

public class CDR_Raw_Data {

    // ================== Attributes ==================
    private int CDR_Raw_Data_CDRID;
    private String CDR_Raw_Data_Caller;
    private String CDR_Raw_Data_Called;
    private LocalDateTime CDR_Raw_Data_StartTime;
    private int CDR_Raw_Data_Duration;
    private String CDR_Raw_Data_ServiceType;
    private int CDR_Raw_Data_FileID;
    private String CDR_Raw_Data_Status;

    // ================== Constructors ==================
    public CDR_Raw_Data() {}
    
    public CDR_Raw_Data(int CDRID, String Caller, String Called, LocalDateTime StartTime,
            int Duration, String ServiceType, int FileID, String Status) {
        this.CDR_Raw_Data_CDRID = CDRID;
        this.CDR_Raw_Data_Caller = Caller;
        this.CDR_Raw_Data_Called = Called;
        this.CDR_Raw_Data_StartTime = StartTime;
        this.CDR_Raw_Data_Duration = Duration;
        this.CDR_Raw_Data_ServiceType = ServiceType;
        this.CDR_Raw_Data_FileID = FileID;
        this.CDR_Raw_Data_Status = Status;
    }

    // ================== Getters ==================
    public int getCDR_Raw_Data_CDRID() {
        return CDR_Raw_Data_CDRID;
    }

    public String getCDR_Raw_Data_Caller() {
        return CDR_Raw_Data_Caller;
    }

    public String getCDR_Raw_Data_Called() {
        return CDR_Raw_Data_Called;
    }

    public LocalDateTime getCDR_Raw_Data_StartTime() {
        return CDR_Raw_Data_StartTime;
    }

    public int getCDR_Raw_Data_Duration() {
        return CDR_Raw_Data_Duration;
    }

    public String getCDR_Raw_Data_ServiceType() {
        return CDR_Raw_Data_ServiceType;
    }

    public int getCDR_Raw_Data_FileID() {
        return CDR_Raw_Data_FileID;
    }

    public String getCDR_Raw_Data_Status() {
        return CDR_Raw_Data_Status;
    }

    // ================== Setters ==================
    public void setCDR_Raw_Data_CDRID(int CDR_Raw_Data_CDRID) {
        this.CDR_Raw_Data_CDRID = CDR_Raw_Data_CDRID;
    }

    public void setCDR_Raw_Data_Caller(String CDR_Raw_Data_Caller) {
        this.CDR_Raw_Data_Caller = CDR_Raw_Data_Caller;
    }

    public void setCDR_Raw_Data_Called(String CDR_Raw_Data_Called) {
        this.CDR_Raw_Data_Called = CDR_Raw_Data_Called;
    }

    public void setCDR_Raw_Data_StartTime(LocalDateTime CDR_Raw_Data_StartTime) {
        this.CDR_Raw_Data_StartTime = CDR_Raw_Data_StartTime;
    }

    public void setCDR_Raw_Data_Duration(int CDR_Raw_Data_Duration) {
        this.CDR_Raw_Data_Duration = CDR_Raw_Data_Duration;
    }

    public void setCDR_Raw_Data_ServiceType(String CDR_Raw_Data_ServiceType) {
        this.CDR_Raw_Data_ServiceType = CDR_Raw_Data_ServiceType;
    }

    public void setCDR_Raw_Data_FileID(int CDR_Raw_Data_FileID) {
        this.CDR_Raw_Data_FileID = CDR_Raw_Data_FileID;
    }

    public void setCDR_Raw_Data_Status(String CDR_Raw_Data_Status) {
        this.CDR_Raw_Data_Status = CDR_Raw_Data_Status;
    }
    // ================== Methods =====================
    // ================================================

    // ================== CSV Reader ==================
    public static List<CDR_Raw_Data> ReadCSV(File file, int fileId) {

        List<CDR_Raw_Data> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String line;
            
            br.readLine();

            while ((line = br.readLine()) != null) {

                try {
                    String[] fields = line.split(",");

                    if (fields.length < 5) {
                        System.err.println("Invalid record: " + line);
                        continue;
                    }
                    for (int i = 0; i < fields.length; i++) {
                        fields[i] = fields[i].replace("\"", "").trim();
                    }
                    
                    CDR_Raw_Data cdr = new CDR_Raw_Data();
                    cdr.setCDR_Raw_Data_Caller(fields[0].trim());
                    cdr.setCDR_Raw_Data_Called(fields[1].trim());
                    cdr.setCDR_Raw_Data_StartTime(LocalDateTime.parse(fields[2].trim()));
                    cdr.setCDR_Raw_Data_Duration(Integer.parseInt(fields[3].trim()));
                    cdr.setCDR_Raw_Data_ServiceType(fields[4].trim());
                    cdr.setCDR_Raw_Data_FileID(fileId);
                    if (fields.length > 6) {
                        cdr.setCDR_Raw_Data_Status(fields[6].trim());
                    } else {
                        cdr.setCDR_Raw_Data_Status("NEW");
                    }
                    list.add(cdr);
                } catch (Exception e) {
                    System.err.println("Error parsing line: " + line);
                    System.err.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + file.getName());
            e.printStackTrace();
        }
        return list;
    }
    // ================== insert CDR record filed ==================

    public static void InsertCDRRecordFiled(CDR_Raw_Data record) 
    {
        String sql = "SELECT insert_cdr(?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = BillingDB.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, record.getCDR_Raw_Data_Caller());
            stmt.setString(2, record.getCDR_Raw_Data_Called());
            stmt.setTimestamp(3, Timestamp.valueOf(record.getCDR_Raw_Data_StartTime()));
            stmt.setInt(4, record.getCDR_Raw_Data_Duration());
            stmt.setString(5, record.getCDR_Raw_Data_ServiceType());
            stmt.setInt(6, record.getCDR_Raw_Data_FileID());
            stmt.setString(7, record.getCDR_Raw_Data_Status());
            stmt.execute();
            con.close();
        } 
        catch (Exception e) 
        {
            System.err.println("Error inserting CDR: " + e.getMessage());
        }
    }
}
