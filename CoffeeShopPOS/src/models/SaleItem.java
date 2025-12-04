package models;

public class SaleItem {
    private int id;
    private int saleId;
    private int productId;
    private int quantity;
    private double unitPrice;
    private double subtotal;
    
    public SaleItem(int id, int saleId, int productId, int quantity, double unitPrice, double subtotal) {
        this.id = id;
        this.saleId = saleId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }
    
    // Getters
    public int getId() { return id; }
    public int getSaleId() { return saleId; }
    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getSubtotal() { return subtotal; }
    
    // Setters (if needed)
    public void setId(int id) { this.id = id; }
    public void setSaleId(int saleId) { this.saleId = saleId; }
    public void setProductId(int productId) { this.productId = productId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}