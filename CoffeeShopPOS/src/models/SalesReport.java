package models;

import java.time.LocalDateTime;

/**
 * Model class for sales report data
 * Combines sale information with cashier and product details
 */
public class SalesReport {
    private int saleId;
    private int cashierId;
    private String cashierUsername;
    private double totalAmount;
    private double discountAmount;
    private double finalAmount;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private int itemCount;
    
    public SalesReport(int saleId, int cashierId, String cashierUsername, 
                      double totalAmount, double discountAmount, double finalAmount,
                      String paymentMethod, LocalDateTime createdAt, int itemCount) {
        this.saleId = saleId;
        this.cashierId = cashierId;
        this.cashierUsername = cashierUsername;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.paymentMethod = paymentMethod;
        this.createdAt = createdAt;
        this.itemCount = itemCount;
    }
    
    // Getters
    public int getSaleId() { return saleId; }
    public int getCashierId() { return cashierId; }
    public String getCashierUsername() { return cashierUsername; }
    public double getTotalAmount() { return totalAmount; }
    public double getDiscountAmount() { return discountAmount; }
    public double getFinalAmount() { return finalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getItemCount() { return itemCount; }
    
    // Setters
    public void setSaleId(int saleId) { this.saleId = saleId; }
    public void setCashierId(int cashierId) { this.cashierId = cashierId; }
    public void setCashierUsername(String cashierUsername) { this.cashierUsername = cashierUsername; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }
}
