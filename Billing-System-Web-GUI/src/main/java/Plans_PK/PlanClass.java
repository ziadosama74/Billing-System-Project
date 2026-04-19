package Plans_PK;
import DB_PK.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
public class PlanClass 
{
    private int planid;
    private String planname;
    private int monthlyfee;
    private String description;
    private Boolean isactive;
    private int data_allowance;
    private int sms_allowance;
    private int voice_allowance;
    private int data_free;
    private int sms_free;
    private int voice_free;
    private String color;
    //=================     Getters          ===================================
    
    public String getColor()
    {
        return color;
    }

    public int getPlanid() {
        return planid;
    }
    
    
    public String getPlanname() {
        return planname;
    }
    
    public int getVoice_free() {
        return voice_free;
    }
    
    public int getMonthlyfee() {
        return monthlyfee;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Boolean getIsactive() {
        return isactive;
    }
    
    public int getData_allowance() {
        return data_allowance;
    }
    
    public int getSms_allowance() {
        return sms_allowance;
    }
    
    public int getVoice_allowance() {
        return voice_allowance;
    }
    
    public int getData_free() {
        return data_free;
    }
    
    public int getSms_free() {
        return sms_free;
    }
    
    //====================          Setters           ==========================
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public void setPlanname(String planname) {
        this.planname = planname;
    }
    
    public void setMonthlyfee(int monthlyfee) {
        this.monthlyfee = monthlyfee;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIsactive(Boolean isactive) {
        this.isactive = isactive;
    }

    public void setData_allowance(int data_allowance) {
        this.data_allowance = data_allowance;
    }

    public void setSms_allowance(int sms_allowance) {
        this.sms_allowance = sms_allowance;
    }

    public void setVoice_allowance(int voice_allowance) {
        this.voice_allowance = voice_allowance;
    }

    public void setData_free(int data_free) {
        this.data_free = data_free;
    }

    public void setSms_free(int sms_free) {
        this.sms_free = sms_free;
    }

    private void setPlanid(int planid) {
        this.planid = planid;
    }
    public void setVoice_free(int voice_free) {
        this.voice_free = voice_free;
    }
    
    //=================     Methods          ===================================
    
    // =========================================================================
    // ============           Get All Plans                      ===============
    // =========================================================================
    
    public static List<PlanClass> GetAllPlans()
    {
        List<PlanClass> PlansList = new ArrayList<>();
        try 
        {
            Connection con = DB.getConnection();
            String sql = "select * from GetAllPlans()";
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet RS = stmt.executeQuery();
            while(RS.next())
            {
                PlanClass plan = new PlanClass();
                plan.setPlanid(RS.getInt("planid"));
                plan.setPlanname(RS.getString("planname"));
                plan.setMonthlyfee(RS.getInt("monthlyfee"));
                plan.setDescription(RS.getString("description"));
                plan.setIsactive(RS.getBoolean("isactive"));
                plan.setData_allowance(RS.getInt("data_allowance"));
                plan.setSms_allowance(RS.getInt("sms_allowance"));
                plan.setVoice_allowance(RS.getInt("voice_allowance"));
                plan.setData_free(RS.getInt("data_free"));
                plan.setSms_free(RS.getInt("sms_free"));
                plan.setVoice_free(RS.getInt("voice_free"));
                plan.setColor(RS.getString("color"));
                PlansList.add(plan);
            }
            RS.close();
            stmt.close();
            con.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return PlansList;
    }
}
