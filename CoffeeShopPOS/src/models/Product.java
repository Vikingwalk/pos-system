
// Enhanced Product.java
package models;

public class Product {
    private int id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private String category;
    private boolean isAvailable;
    private String barcode;
    
    public Product(int id, String name, String description, double price, 
                  int stock, String category, boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.isAvailable = isAvailable;
        this.barcode = null;
    }
    
    public Product(int id, String name, String description, double price, 
                  int stock, String category, boolean isAvailable, String barcode) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.isAvailable = isAvailable;
        this.barcode = barcode;
    }
    
    // Validation methods
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() && 
               price >= 0 && stock >= 0;
    }
    
    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public String getCategory() { return category; }
    public boolean isAvailable() { return isAvailable; }
    public String getBarcode() { return barcode; }
    
    // Setters
    public void setBarcode(String barcode) { this.barcode = barcode; }
}
