package DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BillingDB 
{
    private static final String URL = "jdbc:postgresql://ep-autumn-dust-anzov351-pooler.c-6.us-east-1.aws.neon.tech:5432/neondb?sslmode=require&channel_binding=require";
    private static final String USER = "neondb_owner";
    private static final String PASSWORD = "npg_UqW3DZAinVE6";
    
    public static Connection getConnection() {
        Connection conn = null;

        try 
        {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
         
        } 
        catch (SQLException e) 
        {
            System.out.println("❌ Connection failed!");
            e.printStackTrace();
        }
        return conn;
    }
}
