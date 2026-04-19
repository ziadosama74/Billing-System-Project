package Subscribers_PK;

import java.sql.Date;
import DB_PK.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SubscriberClass 
{
    // ==================== Attributes =========================================
    private int ID;
    private String MSISDN;
    private String Name;
    private String International_ID;
    private String Address;
    private Date Createdat;
    private boolean Status;
    private String Plan;
    
    // ==================== Setters ============================================
    public void setID(int ID) {
        this.ID = ID;
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

    public static List<SubscriberClass> GetAllSubscriber()
    {
        List<SubscriberClass> SubscribersList = new ArrayList<>();
        try
        {
            Connection con = DB.getConnection();
            String sql = "select * from GetAllSubscriber()";
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet RS = stmt.executeQuery();
            while(RS.next())
            {
                SubscriberClass Subscriber = new SubscriberClass();
                Subscriber.setID(RS.getInt("subscriberid"));
                Subscriber.setMSISDN(RS.getString("msisdn"));
                Subscriber.setName(RS.getString("name"));
                Subscriber.setInternational_ID(RS.getString("internationalid"));
                Subscriber.setAddress(RS.getString("address"));
                Subscriber.setStatus(RS.getBoolean("isdeleted"));
                Subscriber.setPlan(RS.getString("plan"));
                SubscribersList.add(Subscriber);
            }
            RS.close();
            stmt.close();
            con.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return SubscribersList;
    }
    // =========================================================================
    // ============           Get Subscriber by MSISDN           ===============
    // =========================================================================
    public SubscriberClass GetSubscriberByMSISDN(String MSISDN) 
    {
        SubscriberClass subscriber = null;
        try 
        {
            Connection con = DB.getConnection();
            String sql = "select * from GetSubscriberByMSISDN(?)";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, MSISDN);
            ResultSet RS = stmt.executeQuery();

            if (RS.next()) 
            {
                subscriber = new SubscriberClass();
                subscriber.setID(RS.getInt("subscriberid"));
                subscriber.setMSISDN(RS.getString("msisdn"));
                subscriber.setName(RS.getString("name"));
                subscriber.setInternational_ID(RS.getString("internationalid"));
                subscriber.setAddress(RS.getString("address"));
                subscriber.setStatus(RS.getBoolean("isdeleted"));
                subscriber.setPlan(RS.getString("plan"));
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
