package models;

public class PromoCode {
    private int id;
    private String code;
    private double discountPercent;
    private boolean isActive;
    
    public PromoCode(int id, String code, double discountPercent, boolean isActive) {
        this.id = id;
        this.code = code;
        this.discountPercent = discountPercent;
        this.isActive = isActive;
    }
    
    public PromoCode(String code, double discountPercent, boolean isActive) {
        this(0, code, discountPercent, isActive);
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(double discountPercent) { this.discountPercent = discountPercent; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }
    
    @Override
    public String toString() {
        return code + " (" + discountPercent + "%)";
    }
}

