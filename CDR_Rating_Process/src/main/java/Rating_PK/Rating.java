
package Rating_PK;
import DB.BillingDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
public class Rating 
{
    private int FileID ;
    public Rating(int FileID)
    {
        this.FileID = FileID;
    }
    public int Rating_Get_File_ID()
    {
        return this.FileID;
    }
    public void StartRating(int FileID)
    {
        try
        {
            Connection con = BillingDB.getConnection();
            String Sql = "select process_cdrs_and_generate_invoice(?)";
            PreparedStatement stmt = con.prepareStatement(Sql);
            stmt.setInt(1, FileID);
            stmt.execute();
            con.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
