package Subscribers_PK;

import java.sql.Date;
import DB_PK.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SubscriberClass {

    // ==================== Attributes =========================================
    private int ID;
    private String MSISDN;
    private String Name;
    private String International_ID;
    private String Address;
    private Date Createdat;
    private boolean Status; 
    private String Plan;
    private int PlanID;

    // ==================== Setters ============================================
    private void setID(int ID) {
        this.ID = ID;
    }
    
    public void setPlanID(int PlanID) {
        this.PlanID = PlanID;
    }
    
    public void setMSISDN(String MSISDN) {
        this.MSISDN = MSISDN;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public void setInternational_ID(String International_ID) {
        this.International_ID = International_ID;
    }

    public void setAddress(String Address) {
        this.Address = Address;
    }

    public void setCreatedat(Date Createdat) {
        this.Createdat = Createdat;
    }

    public void setStatus(boolean Status) {
        this.Status = Status;
    }

    public void setPlan(String Plan) {
        this.Plan = Plan;
    }

    // ==================== Getters ============================================ 
    public int getID() {
        return ID;
    }
    
    public int getPlanID() {
        return PlanID;
    }
    
    public String getMSISDN() {
        return MSISDN;
    }

    public String getName() {
        return Name;
    }

    public String getInternational_ID() {
        return International_ID;
    }

    public String getAddress() {
        return Address;
    }

    public Date getCreatedat() {
        return Createdat;
    }

    public boolean isStatus() {
        return Status;
    }

    public String getPlan() {
        return Plan;
    }
    // ============================ Methods ====================================

    // =========================================================================
    // ============           Get All Subscribers                ===============
    // =========================================================================
    public static List<SubscriberClass> GetAllSubscriber() {
        List<SubscriberClass> SubscribersList = new ArrayList<>();
        try {
            Connection con = DB.getConnection();
            String sql = "select * from GetAllSubscriber()";
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet RS = stmt.executeQuery();
            while (RS.next()) {
                SubscriberClass Subscriber = new SubscriberClass();
                Subscriber.setID(RS.getInt("subscriberid"));
                Subscriber.setMSISDN(RS.getString("msisdn"));
                Subscriber.setName(RS.getString("name"));
                Subscriber.setInternational_ID(RS.getString("internationalid"));
                Subscriber.setAddress(RS.getString("address"));
                Subscriber.setStatus(RS.getBoolean("isactive"));
                Subscriber.setPlan(RS.getString("plan"));
                SubscribersList.add(Subscriber);
            }
            RS.close();
            stmt.close();
            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return SubscribersList;
    }

    // =========================================================================
    // ============           Get Subscriber by MSISDN           ===============
    // =========================================================================
    public SubscriberClass GetSubscriberByMSISDN(String MSISDN) {
        SubscriberClass subscriber = null;
        try {
            Connection con = DB.getConnection();
            String sql = "select * from GetSubscriberByMSISDN(?)";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, MSISDN);
            ResultSet RS = stmt.executeQuery();

            if (RS.next()) {
                subscriber = new SubscriberClass();
                subscriber.setID(RS.getInt("subscriberid"));
                subscriber.setMSISDN(RS.getString("msisdn"));
                subscriber.setName(RS.getString("name"));
                subscriber.setInternational_ID(RS.getString("internationalid"));
                subscriber.setAddress(RS.getString("address"));
                subscriber.setStatus(RS.getBoolean("isactive"));
                subscriber.setPlan(RS.getString("plan"));
            }
            RS.close();
            stmt.close();
            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return subscriber;
    }

    // =========================================================================
    // ============             Add New Subscriber               ===============
    // =========================================================================
    public int AddNewSubscriber(SubscriberClass Subscriber) 
    {
        int SubscriberID = -1;

        try {
            Connection con = DB.getConnection();
            String sql = "SELECT * FROM create_subscriber(?, ?, ?, ?, ?)";
            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.setString(1, Subscriber.getMSISDN());
            stmt.setString(2, Subscriber.getName());
            stmt.setString(3, Subscriber.getInternational_ID());
            stmt.setString(4, Subscriber.getAddress());
            stmt.setInt(5, Subscriber.getPlanID());

            ResultSet RS = stmt.executeQuery();

            if (RS.next()) 
            {
                SubscriberID = RS.getInt(1);
                if (SubscriberID == -1) {
                    System.out.println("Error: International ID already exists: " + Subscriber.getInternational_ID());
                } else if (SubscriberID == -2) {
                    System.out.println("Error: MSISDN already exists: " + MSISDN);
                } else if (SubscriberID > 0) {
                    System.out.println("Success: Subscriber created with ID: " + SubscriberID);
                }
            }
            RS.close();
            stmt.close();
            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return SubscriberID;
    }
    // =========================================================================
    // ============             Delete  Subscriber               ===============
    // =========================================================================
     public boolean softDeleteSubscriber(int subscriberId) {
        boolean success = false;
        Connection con = null;
        PreparedStatement stmt = null;
        try 
        {
            con = DB.getConnection();
   
            String sql = "select * from DeleteSoftSubscriberByID(?)";
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, subscriberId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                success = true;
            }

        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
        } 
        finally 
        {
            try 
            {
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } 
            catch (Exception ex) 
            {
                ex.printStackTrace();
            }
        }
        return success;
    }
    // =========================================================================
    // ============             Get Subscriber by ID               =============
    // =========================================================================
    public SubscriberClass GetSubscriberByID(int id)
    {
        SubscriberClass subscriber = null;
        try
        {
            Connection con = DB.getConnection();
            String sql = "select * from GetSubscriberByID(?)";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet RS = stmt.executeQuery();
            if(RS.next())
            {
                subscriber = new SubscriberClass();
                subscriber.setID(RS.getInt("subscriberid"));
                subscriber.setMSISDN(RS.getString("msisdn"));
                subscriber.setName(RS.getString("name"));
                subscriber.setInternational_ID(RS.getString("internationalid"));
                subscriber.setAddress(RS.getString("address"));
                subscriber.setStatus(RS.getBoolean("isactive"));
                subscriber.setPlan(RS.getString("plan")); 
                subscriber.setPlanID(RS.getInt("planid"));
            }
            RS.close();
            stmt.close();
            con.close();
        }
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }
        return subscriber;
    }
}
