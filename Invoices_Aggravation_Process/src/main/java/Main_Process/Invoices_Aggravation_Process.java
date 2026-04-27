package Main_Process;

import Billing.MonthlyBillingAggregationJob;

public class Invoices_Aggravation_Process {

    public static void main(String[] args) {
        System.out.println("🚀 Billing System Started...");
        
        
        MonthlyBillingAggregationJob.runMonthlyAggregation();
        
        System.out.println("🏁 Finished.");
    }
}