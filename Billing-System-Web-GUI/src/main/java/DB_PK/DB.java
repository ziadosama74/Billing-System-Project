package DB_PK;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {

    private static final String URL =
        "jdbc:postgresql://ep-autumn-dust-anzov351-pooler.c-6.us-east-1.aws.neon.tech:5432/neondb?sslmode=require";

    private static final String USER = "neondb_owner";
    private static final String PASSWORD = "npg_UqW3DZAinVE6";

    public static Connection getConnection()
    {
        try
        {
            // 🔥 Force load driver (fixes “No suitable driver”)
            Class.forName("org.postgresql.Driver");

            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("✅ DB CONNECTED");

            return conn;
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("❌ PostgreSQL Driver NOT FOUND");
            e.printStackTrace();
        }
        catch (SQLException e)
        {
            System.out.println("❌ DB Connection FAILED");
            e.printStackTrace();
        }

        return null;
    }
}