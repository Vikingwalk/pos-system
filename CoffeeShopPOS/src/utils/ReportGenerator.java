// ReportGenerator.java for reporting feature
package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class ReportGenerator {
    
    public static void generateSalesReport(LocalDate startDate, LocalDate endDate) {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = """
                SELECT 
                    DATE(created_at) as sale_date,
                    COUNT(*) as total_sales,
                    SUM(final_amount) as total_revenue,
                    AVG(final_amount) as average_sale
                FROM sales 
                WHERE created_at BETWEEN ? AND ?
                GROUP BY DATE(created_at)
                ORDER BY sale_date
                """;
            
            pst = conn.prepareStatement(sql);
            pst.setDate(1, java.sql.Date.valueOf(startDate));
            pst.setDate(2, java.sql.Date.valueOf(endDate));
            
            rs = pst.executeQuery();
            
            System.out.println("Sales Report: " + startDate + " to " + endDate);
            System.out.println("Date\t\tSales\tRevenue\tAvg Sale");
            System.out.println("----------------------------------------");
            
            while (rs.next()) {
                System.out.printf("%s\t%d\t$%.2f\t$%.2f%n",
                    rs.getDate("sale_date"),
                    rs.getInt("total_sales"),
                    rs.getDouble("total_revenue"),
                    rs.getDouble("average_sale"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(conn, pst, rs);
        }
    }
}