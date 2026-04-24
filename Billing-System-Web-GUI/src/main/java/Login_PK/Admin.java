package Login_PK;

import DB_PK.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import Invoice_PK.InvoiceClass;
import Subscribers_PK.SubscriberClass;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Admin 
{
    private String userName;
    private String password;
    private String fullName;
    private int id;

    // ===== Constructor =====
    public Admin() {}

    public Admin(String userName, String password, String fullName, int id)
    {
        this.userName = userName;
        this.password = password;
        this.fullName = fullName;
        this.id = id;
    }

    // ===== Getters =====
    public String getUserName() { return userName; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public int getID() { return id; }

    // ===== Setters =====
    public void setUserName(String userName) { this.userName = userName; }
    public void setPassword(String password) { this.password = password; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setID(int id) { this.id = id; }

    // ===== LOGIN METHOD =====
    public static Admin login(String userName, String password)
    {
        Admin user = null;

        try
        {
            Connection con = DB.getConnection();

            if (con == null)
            {
                System.out.println("❌DB Connection Failed");
                return null;
            }

            String sql = "SELECT * FROM LoginAdmin(?, ?)";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, userName);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next())
            {
                System.out.println("✅User FOUND");

                user = new Admin();
                user.setID(rs.getInt("id"));
                user.setFullName(rs.getString("fullname"));
                user.setUserName(rs.getString("username"));
            }
            else
            {
                System.out.println("❌User NOT FOUND");
            }
            
            rs.close();
            stmt.close();
            con.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        return user;
    }
    // ===== View Parameters =====
    public static void GetParameters() throws Exception
    {
        int Bills = InvoiceClass.Invoice_Get_No_OF_Bills();
        int Subscribers = SubscriberClass.GetNumOfSubscribers();
        double Revenue = InvoiceClass.Invoice_Get_Revenue_Monthly();
        Map<String, Object> MAP = new HashMap<>();
        MAP.put("Bills", Bills);
        MAP.put("Subscribers", Subscribers);
        MAP.put("Revenue", Revenue);
        String home  = System.getProperty("user.home");
        String JsonPath = home + "/Billing-System-Project/Billing-System-Web-GUI/src/main/webapp/JSON/parameters.json";
        ObjectMapper  data = new ObjectMapper();
        data.writeValue(new File(JsonPath), MAP);
    }
}
