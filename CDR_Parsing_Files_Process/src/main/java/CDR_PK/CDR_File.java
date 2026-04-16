package CDR_PK;

import java.time.LocalDateTime;
import DB.BillingDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CDR_File {
    // ================== Attributes ==================
    private int CDR_File_ID;
    private String CDR_File_Name;
    private LocalDateTime CDR_File_ReceivedAt;
    private LocalDateTime CDR_File_ProcessedAt;
    private String CDR_File_Status;
    
    // ================== Constructors ==================
    public CDR_File() {
    }
    
    public CDR_File(String CDR_File_Name) {
        this.CDR_File_Name = CDR_File_Name;
    }

    // ================== Getters ==================
    public int getCDR_File_ID() {
        return CDR_File_ID;
    }

    public String getCDR_File_Name() {
        return CDR_File_Name;
    }

    public LocalDateTime getCDR_File_ReceivedAt() {
        return CDR_File_ReceivedAt;
    }

    public LocalDateTime getCDR_File_ProcessedAt() {
        return CDR_File_ProcessedAt;
    }

    public String getCDR_File_Status() {
        return CDR_File_Status;
    }

    // ================== Setters ==================
    public void setCDR_File_Name(String CDR_File_Name) {
        this.CDR_File_Name = CDR_File_Name;
    }

    public void setCDR_File_ReceivedAt(LocalDateTime CDR_File_ReceivedAt) {
        this.CDR_File_ReceivedAt = CDR_File_ReceivedAt;
    }

    public void setCDR_File_ProcessedAt(LocalDateTime CDR_File_ProcessedAt) {
        this.CDR_File_ProcessedAt = CDR_File_ProcessedAt;
    }

    public void setCDR_File_Status(String CDR_File_Status) {
        this.CDR_File_Status = CDR_File_Status;
    }

    // ================== Methods ==================
    public int CDR_File_Insert_Recived_File(String filename) {
        int fileId = -1;
        try 
        {
            Connection con = (Connection) BillingDB.getConnection();
            String sql = "SELECT insert_cdr_file(?)";
            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.setString(1, filename);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                fileId = rs.getInt(1);
            }
            con.close();
        } catch (Exception e) {

            e.printStackTrace();
        }
        return fileId;
    }
}
