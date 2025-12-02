package models;

public class CartItem {
    private Product product;
    private int quantity;
    private double subtotal;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.subtotal = product.getPrice() * quantity;
    }

    // Getters and setters
    public Product getProduct() { return product; }
    public void setProduct(Product product) { 
        this.product = product; 
        this.subtotal = product.getPrice() * quantity;
    }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity; 
        this.subtotal = product.getPrice() * quantity;
    }
    
    public double getSubtotal() { return subtotal; }
    
    // Property methods for TableView
    public String getProductName() { return product.getName(); }
    public double getProductPrice() { return product.getPrice(); }
}