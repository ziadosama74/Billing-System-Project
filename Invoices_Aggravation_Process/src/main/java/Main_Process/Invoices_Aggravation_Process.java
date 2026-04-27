package Main_Process;

import Billing.MonthlyBillingAggregationJob; // شيل الـ .java من هنا

public class Invoices_Aggravation_Process {

    public static void main(String[] args) {
        System.out.println("🚀 Billing System Started...");
        
        // استدعاء الميثود صح
        MonthlyBillingAggregationJob.runMonthlyAggregation();
        
        System.out.println("🏁 Finished.");
    }
}