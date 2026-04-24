package Invoice_PK;

import DB_PK.DB;
import Subscribers_PK.SubscriberClass;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class InvoiceClass extends SubscriberClass
{
    //====================== Attributes ========================================
    
    private int Invoice_ID;
    private String Invoice_Month;
    private Date Invoice_Due_date;
    private Double Invoice_Total_Amount;
    
    //======================   Getter   ========================================

    public int getInvoice_ID() {
        return Invoice_ID;
    }

    public String getInvoice_Month() {
        return Invoice_Month;
    }

    public Date getInvoice_Due_date() {
        return Invoice_Due_date;
    }

    public Double getInvoice_Total_Amount() {
        return Invoice_Total_Amount;
    }

    //======================   Setter   ========================================

    public void setInvoice_ID(int invoice_ID) {
        this.Invoice_ID = invoice_ID;
    }

    public void setInvoice_Month(String invoice_Month) {
        this.Invoice_Month = invoice_Month;
    }

    public void setInvoice_Due_date(Date invoice_Due_date) {
        this.Invoice_Due_date = invoice_Due_date;
    }

    public void setInvoice_Total_Amount(Double invoice_Total_Amount) {
        this.Invoice_Total_Amount = invoice_Total_Amount;
    }
    
    //========================  Constractors ===================================
    
    public InvoiceClass(){}
    public InvoiceClass(int IN_ID){this.Invoice_ID = IN_ID;}
    
    //========================   Methods     ===================================
    
    public static List<InvoiceClass> Invoice_Get_All_Invoices()
    {
        List<InvoiceClass> InvoicesList = new ArrayList<>();
        try
        {
            Connection con = DB.getConnection();
            String sql = "select * from get_invoices()";
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet RS = stmt.executeQuery();
            
            while(RS.next())
            {
                InvoiceClass Invoice = new InvoiceClass(RS.getInt("invoiceid"));
                Invoice.setName(RS.getString("name"));
                Invoice.setMSISDN(RS.getString("msisdn"));
                Invoice.setInvoice_Month(RS.getString("month_name"));
                Invoice.setInvoice_Due_date(RS.getDate("due_date"));
                Invoice.setInvoice_Total_Amount(RS.getDouble("totalamount"));
                Invoice.setID(RS.getInt("subscriberid"));
                Invoice.setAddress(RS.getString("address"));
                Invoice.setPlan(RS.getString("planname"));
                InvoicesList.add(Invoice);
            }
        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }
        return InvoicesList;
    }
    
    public static int Invoice_Get_No_OF_Bills()
    {
        int NO_OF_Bills = 0 ;
        try
        {
            Connection con = DB.getConnection();
            String sql= "select * from GetNumOfBillsMonthly()";
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet RS = stmt.executeQuery();
            if(RS.next())
            {
                NO_OF_Bills = RS.getInt(1);
            }
            RS.close();
            stmt.close();
        }
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }
        return NO_OF_Bills;
    }
    
    public static double Invoice_Get_Revenue_Monthly()
    {
        double Revenue = 0 ;
        try
        {
            Connection con = DB.getConnection();
            String sql= "select * from GetRevenue()";
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet RS = stmt.executeQuery();
            if(RS.next())
            {
                Revenue = RS.getDouble(1);
            }
            RS.close();
            stmt.close();
        }
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }
        return Revenue;
    }
}
