package models;

import java.time.LocalDateTime;
import java.util.List;

public class Sale {
    private int id;
    private int cashierId;
    private double totalAmount;
    private double discountAmount;
    private double finalAmount;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private List<SaleItem> items;
    
    public Sale(int id, int cashierId, double totalAmount, double discountAmount,
                double finalAmount, String paymentMethod, LocalDateTime createdAt) {
        this.id = id;
        this.cashierId = cashierId;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.paymentMethod = paymentMethod;
        this.createdAt = createdAt;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public int getCashierId() { return cashierId; }
    public double getTotalAmount() { return totalAmount; }
    public double getDiscountAmount() { return discountAmount; }
    public double getFinalAmount() { return finalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; }
}