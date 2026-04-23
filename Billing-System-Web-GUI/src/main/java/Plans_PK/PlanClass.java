package Plans_PK;

import DB_PK.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PlanClass {

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

    public PlanClass() {
    }

    public PlanClass(int id) {
        this.planid = id;
    }
    //=================     Getters          ===================================

    public String getColor() {
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
    public static List<PlanClass> GetAllPlans() {
        List<PlanClass> PlansList = new ArrayList<>();
        try {
            Connection con = DB.getConnection();
            String sql = "select * from GetAllPlans()";
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet RS = stmt.executeQuery();
            while (RS.next()) {
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return PlansList;
    }

    public static String GetPlanNameByID(int id) {
        String Planname = "";
        try {
            Connection con = DB.getConnection();
            String sql = "select * from GetPlanNameByID(?)";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet RS = stmt.executeQuery();
            if (RS.next()) {
                Planname = RS.getString(1);
            }
            RS.close();
            stmt.close();
            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Planname;
    }

    public Boolean EditPlanByID(PlanClass plan) {
        Boolean status = false;
        try {
            Connection con = DB.getConnection();

            String sql = "SELECT update_plan(?,?,?,?,?,?,?,?,?,?,?,?)";

            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.setInt(1, plan.getPlanid());
            stmt.setString(2, plan.getPlanname());
            stmt.setInt(3, plan.getMonthlyfee());
            stmt.setString(4, plan.getDescription());
            stmt.setBoolean(5, plan.getIsactive());
            stmt.setString(6, plan.getColor());
            stmt.setInt(7, plan.getVoice_allowance());
            stmt.setInt(8, plan.getData_allowance());
            stmt.setInt(9, plan.getSms_allowance());
            stmt.setInt(10, plan.getVoice_free());
            stmt.setInt(11, plan.getData_free());
            stmt.setInt(12, plan.getSms_free());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                status = rs.getBoolean(1);
            } else {
                System.out.println("No result returned from function");
            }

            rs.close();
            stmt.close();
            con.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error in EditPlanByID: " + ex.getMessage());
        }
        return status;
    }

    public Boolean update_plan_status(int id, Boolean status_plan) {
        Boolean status = false;
        try {
            Connection con = DB.getConnection();

            String sql = "SELECT update_plan_status(?,?)";

            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.setInt(1, id);
            stmt.setBoolean(2, status_plan);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                status = rs.getBoolean(1);
            } else {
                System.out.println("No result returned from function");
            }
            rs.close();
            stmt.close();
            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error in EditPlanByID: " + ex.getMessage());
        }
        return status;
    }

    public Integer InsertPlan(PlanClass plan) {
        Integer newPlanId = null;
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DB.getConnection();

            String sql = "SELECT insert_plan(?, CAST(? AS NUMERIC(10,2)), ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = con.prepareStatement(sql);

            stmt.setString(1, plan.getPlanname());           // p_planname
            stmt.setInt(2, plan.getMonthlyfee());            // p_monthlyfee (cast to NUMERIC)
            stmt.setString(3, plan.getDescription());        // p_description
            stmt.setBoolean(4, plan.getIsactive());          // p_isactive
            stmt.setString(5, plan.getColor());              // p_color
            stmt.setInt(6, plan.getVoice_allowance());       // p_voice_allowance
            stmt.setInt(7, plan.getData_allowance());        // p_data_allowance
            stmt.setInt(8, plan.getSms_allowance());         // p_sms_allowance
            stmt.setInt(9, plan.getVoice_free());            // p_voice_free
            stmt.setInt(10, plan.getData_free());            // p_data_free
            stmt.setInt(11, plan.getSms_free());             // p_sms_free

            rs = stmt.executeQuery();

            if (rs.next()) {
                newPlanId = rs.getInt(1);
                if (rs.wasNull()) {
                    newPlanId = null;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error inserting plan: " + ex.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return newPlanId;
    }
}
