package Billing;

import DB.BillingDB;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.HorizontalImageAlignment;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class MonthlyBillingAggregationJob {

    public static void runMonthlyAggregation() {
        System.out.println("🚀 GENERATING FINAL TEAM 1 INVOICES...");
        String fetchInvoicesSQL = "SELECT * FROM get_invoices()"; 

        try (Connection con = BillingDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(fetchInvoicesSQL);
             ResultSet rs = stmt.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                
                String address = rs.getString("address");
                if (address == null || address.isEmpty()) {
                    address = "Cairo,Egypt"; 
                }

                generateFinalReport(
                    rs.getInt("invoiceid"), 
                    rs.getString("name"), 
                    rs.getString("msisdn"), 
                    address, 
                    rs.getString("month_name").trim(), 
                    rs.getString("planname"), 
                    rs.getDouble("totalamount"), 
                    rs.getDate("due_date").toString(), 
                    con
                );
                count++;
            }
            System.out.println("✅ DONE! " + count + " Final Invoices Generated Successfully.");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void generateFinalReport(int id, String name, String msisdn, String address, String month, String plan, double total, String due, Connection con) {
        try {
            String path = System.getProperty("user.home") + "/Billing-System-Project/Final-Invoices/" + msisdn + "_Final_Invoice.pdf";
            new File(System.getProperty("user.home") + "/Billing-System-Project/Final-Invoices/").mkdirs(); 
            String logoPath = "logo.png";
            
            // colors
            Color navyBlue = new Color(21, 34, 56);
            Color accentRed = new Color(200, 50, 50);
            Color lightGray = new Color(245, 247, 250);

            // styles
            var boldNavy = Styles.style().bold().setForegroundColor(navyBlue);
            var titleStyle = Styles.style().bold().setFontSize(36).setForegroundColor(navyBlue).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
            var team1Style = Styles.style().bold().setFontSize(16).setForegroundColor(navyBlue).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
            
            var grayBoxStyle = Styles.style().setBackgroundColor(lightGray).setPadding(12)
                                     .setBorder(Styles.pen1Point().setLineColor(navyBlue));
            
            var labelStyle = Styles.style().bold().setFontSize(11).setForegroundColor(navyBlue);
            var valueStyle = Styles.style().setFontSize(10).setPadding(2);

            var columnHeaderStyle = Styles.style().bold().setBackgroundColor(navyBlue).setForegroundColor(Color.WHITE)
                                          .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER).setPadding(10);
            
            var columnStyle = Styles.style().setPadding(8).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                                    .setBorder(Styles.border().setBottomPen(Styles.pen1Point().setLineColor(new Color(220, 220, 220))));

            // Query 
            String usageSQL = "SELECT servicetype, " +
                              "CASE " +
                              "  WHEN UPPER(servicetype) LIKE '%VOICE%' THEN usedunits || ' mins' " +
                              "  WHEN UPPER(servicetype) LIKE '%DATA%' THEN usedunits || ' GB' " +
                              "  WHEN UPPER(servicetype) LIKE '%SMS%' THEN usedunits || ' SMS' " +
                              "  ELSE usedunits::text " +
                              "END as formatted_units " +
                              "FROM subscriber_usage WHERE subscriberid = (SELECT subscriberid FROM invoices WHERE invoiceid = " + id + ")";

            // 
            DynamicReports.report()
                .setTemplate(DynamicReports.template())
                .setPageMargin(DynamicReports.margin(40))
                
                // header+logo
                .title(
                    Components.horizontalList(
                        Components.text("").setFixedWidth(130), 
                        Components.verticalList(
                            Components.verticalGap(20), 
                            Components.text("INVOICE").setStyle(titleStyle),
                            Components.text("TEAM 1 OPERATOR").setStyle(team1Style)
                        ),
                        Components.image(logoPath).setFixedDimension(130, 130).setHorizontalImageAlignment(HorizontalImageAlignment.RIGHT)
                    ),
                    Components.verticalGap(40),
                    
                    //
                    Components.horizontalList(
                        Components.text("BILLED TO:").setStyle(labelStyle),
                        Components.horizontalGap(20),
                        Components.text("PAYMENT DETAILS:").setStyle(labelStyle)
                    ),
                    Components.verticalGap(5),
                    
                    // 
                    Components.horizontalList(
                        // 
                        Components.verticalList(
                            Components.text("Name: " + name).setStyle(valueStyle),
                            Components.text("Account #: " + id).setStyle(valueStyle),
                            Components.text("Phone #: " + msisdn).setStyle(valueStyle),
                            Components.text("Address: " + address).setStyle(valueStyle),
                            Components.text(" ").setStyle(valueStyle) // 👈 
                        ).setStyle(grayBoxStyle),
                        Components.horizontalGap(20), 
                        
                        // 
                        Components.verticalList(
                            Components.text("Invoice ID: #" + id).setStyle(valueStyle),
                            Components.text("Date: " + LocalDate.now()).setStyle(valueStyle),
                            Components.text("Rate Plan: " + plan).setStyle(valueStyle),
                            Components.text("Billing Period: " + month).setStyle(valueStyle),
                            Components.text("Due Date: " + due).setStyle(Styles.style(valueStyle).bold().setForegroundColor(accentRed))
                        ).setStyle(grayBoxStyle)
                    ),
                    Components.verticalGap(30)
                )
                
                //
                .columns(
                    Columns.reportRowNumberColumn("NO.").setFixedWidth(40).setStyle(columnStyle),
                    Columns.column("DESCRIPTION", "servicetype", DynamicReports.type.stringType()).setFixedWidth(250).setStyle(columnStyle),
                    Columns.column("UNITS CONSUMED", "formatted_units", DynamicReports.type.stringType()).setStyle(columnStyle)
                )
                .setColumnTitleStyle(columnHeaderStyle)
                .setDataSource(usageSQL, con)
                
                //
                .summary(
                    Components.verticalGap(30),
                    Components.verticalList(
                        Components.text("TOTAL DUE AMOUNT")
                            .setStyle(Styles.style().bold().setHorizontalTextAlignment(HorizontalTextAlignment.CENTER).setFontSize(12).setForegroundColor(navyBlue)),
                        Components.text(total + " EGP")
                            .setStyle(Styles.style().bold().setHorizontalTextAlignment(HorizontalTextAlignment.CENTER).setFontSize(26).setForegroundColor(accentRed))
                    ).setStyle(Styles.style().setBackgroundColor(lightGray).setPadding(15).setBorder(Styles.pen1Point().setLineColor(navyBlue))),
                    Components.verticalGap(40)
                )
                
                //
                .lastPageFooter(
                    Components.line().setPen(Styles.pen1Point().setLineColor(Color.LIGHT_GRAY)),
                    Components.verticalGap(10),
                    Components.horizontalList(
                        Components.text("Customer Care: 112").setStyle(Styles.style().setFontSize(9).setForegroundColor(navyBlue)),
                        Components.text("Email: team1@iti.gov.eg").setStyle(Styles.style().setFontSize(9).setForegroundColor(navyBlue).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)),
                        Components.text("Web: www.team1-telecom.com").setStyle(Styles.style().setFontSize(9).setForegroundColor(navyBlue).setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT))
                    )
                )
                .show(false)
                .toPdf(new FileOutputStream(path));

            System.out.println("[✔] Invoice Generated: " + path);

        } catch (Exception e) { e.printStackTrace(); }
    }
}