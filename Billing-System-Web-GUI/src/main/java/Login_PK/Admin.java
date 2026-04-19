package Login_PK;

import DB_PK.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
}
