package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Product;
import utils.DBConnection;
import utils.SessionManager;
import utils.BarcodeReceiver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CashierController {
    private TableView<Product> productTable;
    private TextField searchField, productIdField, quantityField, discountField;
    private Button searchButton, addToCartButton, checkoutButton, printReceiptButton, clearCartButton, logoutButton;
    private TableView<Product> cartTable;
    private Label totalLabel, discountLabel, finalTotalLabel;
    
    private ObservableList<Product> productList;
    private ObservableList<Product> cartList;
    private double totalAmount = 0;
    private double discountAmount = 0;
    private boolean productTableInitialized = false;
    
    // Barcode receiver for phone scanning
    private BarcodeReceiver barcodeReceiver;
    
    public CashierController(TableView<Product> productTable, TextField searchField,
            Button searchButton, TableView<Product> cartTable,
            TextField productIdField, TextField quantityField,
            Button addToCartButton, TextField discountField,
            Label totalLabel, Label discountLabel, Label finalTotalLabel,
            Button checkoutButton, Button printReceiptButton,
            Button clearCartButton, Button logoutButton) {


        
        initializeFields(productTable, searchField, productIdField, quantityField,
                        discountField, searchButton, addToCartButton, checkoutButton,
                        printReceiptButton, clearCartButton, cartTable, totalLabel,
                        discountLabel, finalTotalLabel);
        
        initEvents();
        loadProducts();
        setupCartTable();
        updateTotals();
        
        // Start barcode receiver server for phone scanning
        startBarcodeReceiver();
    }
    
    private void initializeFields(TableView<Product> productTable, TextField searchField,
                                 TextField productIdField, TextField quantityField,
                                 TextField discountField, Button searchButton,
                                 Button addToCartButton, Button checkoutButton,
                                 Button printReceiptButton, Button clearCartButton,
                                 TableView<Product> cartTable, Label totalLabel,
                                 Label discountLabel, Label finalTotalLabel) {
        
        this.productTable = productTable;
        this.searchField = searchField;
        this.productIdField = productIdField;
        this.quantityField = quantityField;
        this.discountField = discountField;
        this.searchButton = searchButton;
        this.addToCartButton = addToCartButton;
        this.checkoutButton = checkoutButton;
        this.printReceiptButton = printReceiptButton;
        this.clearCartButton = clearCartButton;
        this.cartTable = cartTable;
        this.totalLabel = totalLabel;
        this.discountLabel = discountLabel;
        this.finalTotalLabel = finalTotalLabel;
        this.logoutButton = logoutButton;
        
        this.productList = FXCollections.observableArrayList();
        this.cartList = FXCollections.observableArrayList();
    }
    
    private void initEvents() {
        searchButton.setOnAction(e -> searchProducts());
        searchField.setOnAction(e -> searchProducts());
        
        addToCartButton.setOnAction(e -> addToCart());
        quantityField.setOnAction(e -> addToCart());
        
        checkoutButton.setOnAction(e -> checkout());
        printReceiptButton.setOnAction(e -> printReceipt());
        clearCartButton.setOnAction(e -> clearCart());
        
        if (logoutButton != null) {
            logoutButton.setOnAction(e -> {
                stopBarcodeReceiver();
                // Logout will be handled by LoginController
            });
        }
        
        discountField.textProperty().addListener((obs, oldVal, newVal) -> applyDiscount());
        
        // Product table selection
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Prefer barcode if available, otherwise use ID
                if (newVal.getBarcode() != null && !newVal.getBarcode().isEmpty()) {
                    productIdField.setText(newVal.getBarcode());
                } else {
                    productIdField.setText(String.valueOf(newVal.getId()));
                }
                quantityField.requestFocus();
            }
        });
    }
    
    private void loadProducts() {
        productList.clear();
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            if (conn == null) {
                System.err.println("CRITICAL: Database connection is NULL!");
                showAlert("Database Error", "Cannot connect to database. Please check MySQL server.");
                return;
            }
            
            System.out.println("Loading products from database...");
            pst = conn.prepareStatement("SELECT * FROM products");
            rs = pst.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                String barcode = null;
                try {
                    barcode = rs.getString("barcode");
                } catch (Exception e) {
                    // Column might not exist yet
                    barcode = null;
                }
                
                Product product = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getInt("stock"),
                    rs.getString("category"),
                    rs.getBoolean("is_available"),
                    barcode
                );
                productList.add(product);
                count++;
                
                // Debug: Print product info
                System.out.println("Loaded product: ID=" + product.getId() + 
                                 ", Name=" + product.getName() + 
                                 ", Barcode=" + product.getBarcode() + 
                                 ", Available=" + product.isAvailable() + 
                                 ", Stock=" + product.getStock());
            }
            
            System.out.println("Cashier: Loaded " + productList.size() + " products total");
            
            if (count == 0) {
                System.err.println("WARNING: No products found in database!");
                showAlert("No Products", "No products found in database. Please add products in Admin dashboard.");
            } else {
                System.out.println("Products loaded successfully, updating table...");
            }
            
            // Update the table with loaded products
            setupProductTable();
            
            System.out.println("Product table now shows " + productTable.getItems().size() + " items");
            
        } catch (Exception e) {
            System.err.println("ERROR loading products: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load products: " + e.getMessage());
        } finally {
            DBConnection.closeResources(conn, pst, rs);
        }
    }
    
    private void setupProductTable() {
        // Only initialize columns once
        if (!productTableInitialized) {
            System.out.println("Initializing product table columns...");
            productTable.getColumns().clear();
            
            TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            idCol.setPrefWidth(50);
            
            TableColumn<Product, String> barcodeCol = new TableColumn<>("Barcode");
            barcodeCol.setCellValueFactory(new PropertyValueFactory<>("barcode"));
            barcodeCol.setPrefWidth(120);
            
            TableColumn<Product, String> nameCol = new TableColumn<>("Name");
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            nameCol.setPrefWidth(130);
            
            TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
            priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
            priceCol.setPrefWidth(70);
            
            TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
            stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
            stockCol.setPrefWidth(50);
            
            productTable.getColumns().addAll(idCol, barcodeCol, nameCol, priceCol, stockCol);
            productTableInitialized = true;
            System.out.println("Product table columns initialized");
        }
        
        // Always refresh the items
        refreshProductTableData();
    }
    
    /**
     * Refresh product table data without rebuilding columns
     */
    private void refreshProductTableData() {
        System.out.println("Refreshing product table with " + productList.size() + " products");
        
        // Set the items
        productTable.setItems(productList);
        
        // Force refresh
        productTable.refresh();
        
        System.out.println("Table refresh complete. Visible items: " + productTable.getItems().size());
    }
    
    /**
     * Refresh products after checkout without clearing the table
     * This prevents the table from appearing empty during reload
     */
    private void refreshProductsAfterCheckout() {
        System.out.println(">>> BEFORE REFRESH: Product table has " + productTable.getItems().size() + " items");
        System.out.println(">>> BEFORE REFRESH: Product list has " + productList.size() + " items");
        
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            if (conn == null) {
                System.err.println("CRITICAL: Database connection is NULL during refresh!");
                return;
            }
            
            System.out.println("Querying updated product data...");
            pst = conn.prepareStatement("SELECT * FROM products");
            rs = pst.executeQuery();
            
            // Create a new list for updated products
            ObservableList<Product> updatedList = FXCollections.observableArrayList();
            
            while (rs.next()) {
                String barcode = null;
                try {
                    barcode = rs.getString("barcode");
                } catch (Exception e) {
                    barcode = null;
                }
                
                Product product = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getInt("stock"),
                    rs.getString("category"),
                    rs.getBoolean("is_available"),
                    barcode
                );
                updatedList.add(product);
            }
            
            System.out.println("Loaded " + updatedList.size() + " updated products");
            
            // Clear and replace productList content
            productList.clear();
            productList.addAll(updatedList);
            
            System.out.println("Updated productList, now has " + productList.size() + " items");
            
            // Refresh the table display
            productTable.refresh();
            
            System.out.println(">>> AFTER REFRESH: Product table has " + productTable.getItems().size() + " items");
            System.out.println(">>> Products successfully refreshed!");
            
        } catch (Exception e) {
            System.err.println("ERROR refreshing products: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(conn, pst, rs);
        }
    }
    
    private void setupCartTable() {
        cartTable.getItems().clear();
        cartTable.getColumns().clear();
        
        TableColumn<Product, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);
        
        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(80);
        
        TableColumn<Product, Integer> quantityCol = new TableColumn<>("Qty");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        quantityCol.setPrefWidth(60);
        
        TableColumn<Product, Double> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleDoubleProperty(
                cellData.getValue().getPrice() * cellData.getValue().getStock()
            ).asObject()
        );
        subtotalCol.setPrefWidth(80);
        
        cartTable.getColumns().addAll(nameCol, priceCol, quantityCol, subtotalCol);
        cartTable.setItems(cartList);
    }
    
    private void searchProducts() {
        String searchTerm = searchField.getText().trim();
        
        System.out.println("Searching for: '" + searchTerm + "'");
        
        if (searchTerm.isEmpty()) {
            productTable.setItems(productList);
            System.out.println("Empty search - showing all " + productList.size() + " products");
            return;
        }
        
        ObservableList<Product> filteredList = FXCollections.observableArrayList();
        for (Product product : productList) {
            boolean matchName = product.getName().toLowerCase().contains(searchTerm.toLowerCase());
            boolean matchId = String.valueOf(product.getId()).equals(searchTerm);
            boolean matchBarcode = (product.getBarcode() != null && product.getBarcode().equals(searchTerm));
            
            if (matchName || matchId || matchBarcode) {
                filteredList.add(product);
                System.out.println("  Match found: " + product.getName() + 
                                 " (ID=" + product.getId() + 
                                 ", Barcode=" + product.getBarcode() + ")");
            }
        }
        
        System.out.println("Search complete: Found " + filteredList.size() + " products");
        productTable.setItems(filteredList);
        
        if (filteredList.isEmpty()) {
            showAlert("No Results", "No products found matching: '" + searchTerm + "'");
        }
    }
    
    private void addToCart() {
        try {
            String input = productIdField.getText().trim();
            int quantity = Integer.parseInt(quantityField.getText());
            
            if (quantity <= 0) {
                showAlert("Input Error", "Quantity must be greater than 0");
                return;
            }
            
            // Find product by ID or barcode
            Product selectedProduct = null;
            
            // First, try to parse as product ID
            try {
                int productId = Integer.parseInt(input);
                for (Product product : productList) {
                    if (product.getId() == productId) {
                        selectedProduct = product;
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                // Not a number, might be a barcode
            }
            
            // If not found by ID, search by barcode
            if (selectedProduct == null) {
                for (Product product : productList) {
                    if (product.getBarcode() != null && product.getBarcode().equals(input)) {
                        selectedProduct = product;
                        break;
                    }
                }
            }
            
            // If still not found, try querying database directly
            if (selectedProduct == null) {
                selectedProduct = findProductByBarcodeOrId(input);
            }
            
            if (selectedProduct == null) {
                showAlert("Error", "Product not found with ID/Barcode: " + input);
                return;
            }
            
            // Check if product is available
            if (!selectedProduct.isAvailable()) {
                showAlert("Product Unavailable", "Product '" + selectedProduct.getName() + "' is currently unavailable.");
                return;
            }
            
            // Check stock
            if (selectedProduct.getStock() <= 0) {
                showAlert("Out of Stock", "Product '" + selectedProduct.getName() + "' is out of stock.");
                return;
            }
            
            if (selectedProduct.getStock() < quantity) {
                showAlert("Stock Error", "Insufficient stock. Available: " + selectedProduct.getStock());
                return;
            }
            
            // Check if product already in cart
            boolean found = false;
            for (int i = 0; i < cartList.size(); i++) {
                Product cartItem = cartList.get(i);
                if (cartItem.getId() == selectedProduct.getId()) {
                    // Update quantity
                    int newQuantity = cartItem.getStock() + quantity;
                    if (newQuantity > selectedProduct.getStock()) {
                        showAlert("Stock Error", "Total quantity exceeds available stock");
                        return;
                    }
                    // Replace the item with updated quantity
                    Product updatedItem = new Product(cartItem.getId(), cartItem.getName(), 
                                         cartItem.getDescription(), cartItem.getPrice(),
                                         newQuantity, cartItem.getCategory(), true);
                    cartList.set(i, updatedItem);
                    cartTable.refresh();
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                // Add new item to cart
                Product cartItem = new Product(selectedProduct.getId(), selectedProduct.getName(),
                                             selectedProduct.getDescription(), selectedProduct.getPrice(),
                                             quantity, selectedProduct.getCategory(), true);
                cartList.add(cartItem);
            }
            
            updateTotals();
            productIdField.clear();
            quantityField.clear();
            productIdField.requestFocus();
            
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter valid numbers for Product ID and Quantity");
        }
    }
    
    private void applyDiscount() {
        try {
            String discountText = discountField.getText().trim();
            if (discountText.isEmpty()) {
                discountAmount = 0;
            } else {
                double discountPercent = Double.parseDouble(discountText);
                if (discountPercent < 0 || discountPercent > 100) {
                    showAlert("Input Error", "Discount must be between 0 and 100");
                    discountField.clear();
                    discountAmount = 0;
                } else {
                    discountAmount = totalAmount * (discountPercent / 100);
                }
            }
            updateTotals();
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter a valid discount percentage");
            discountField.clear();
        }
    }
    
    private void updateTotals() {
        totalAmount = 0;
        for (Product item : cartList) {
            totalAmount += item.getPrice() * item.getStock();
        }
        
        totalLabel.setText(String.format("$%.2f", totalAmount));
        discountLabel.setText(String.format("$%.2f", discountAmount));
        finalTotalLabel.setText(String.format("$%.2f", totalAmount - discountAmount));
    }
    
    private void checkout() {
        if (cartList.isEmpty()) {
            showAlert("Cart Empty", "Please add items to cart before checkout");
            return;
        }
        
        // Process sale in database
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // 1. Insert sale record
            String saleSql = "INSERT INTO sales (cashier_id, total_amount, discount_amount, final_amount) VALUES (?, ?, ?, ?)";
            pst = conn.prepareStatement(saleSql, PreparedStatement.RETURN_GENERATED_KEYS);
            
            // Get actual cashier ID from session
            Integer cashierId = SessionManager.getCurrentUserId();
            if (cashierId == null) {
                showAlert("Error", "No user session found. Please log in again.");
                return;
            }
            
            pst.setInt(1, cashierId);
            pst.setDouble(2, totalAmount);
            pst.setDouble(3, discountAmount);
            pst.setDouble(4, totalAmount - discountAmount);
            pst.executeUpdate();
            
            // Get generated sale ID
            int saleId;
            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    saleId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Failed to get sale ID");
                }
            }
            
            // 2. Insert sale items and update stock
            for (Product item : cartList) {
                // Insert sale item
                String itemSql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
                pst = conn.prepareStatement(itemSql);
                pst.setInt(1, saleId);
                pst.setInt(2, item.getId());
                pst.setInt(3, item.getStock());
                pst.setDouble(4, item.getPrice());
                pst.setDouble(5, item.getPrice() * item.getStock());
                pst.executeUpdate();
                
                // Update product stock
                String updateStockSql = "UPDATE products SET stock = stock - ? WHERE id = ?";
                pst = conn.prepareStatement(updateStockSql);
                pst.setInt(1, item.getStock());
                pst.setInt(2, item.getId());
                pst.executeUpdate();
                
                // Log inventory change
                String logSql = "INSERT INTO inventory_logs (product_id, change_type, quantity_change, previous_stock, new_stock) " +
                               "SELECT ?, 'sale', ?, stock, stock - ? FROM products WHERE id = ?";
                pst = conn.prepareStatement(logSql);
                pst.setInt(1, item.getId());
                pst.setInt(2, -item.getStock());
                pst.setInt(3, item.getStock());
                pst.setInt(4, item.getId());
                pst.executeUpdate();
            }
            
            conn.commit(); // Commit transaction
            
            System.out.println("=== CHECKOUT SUCCESSFUL ===");
            System.out.println("Sale ID: " + saleId);
            System.out.println("Cashier ID: " + cashierId);
            System.out.println("Total: $" + (totalAmount - discountAmount));
            System.out.println("===========================");
            
            // Show success message
            showAlert("Success", "Sale completed successfully! Sale ID: " + saleId);
            
            // Print receipt
            printReceipt();
            
            // Clear cart for next transaction
            clearCart();
            
            // Refresh product list to show updated stock
            System.out.println("Refreshing product list after checkout...");
            refreshProductsAfterCheckout();
            System.out.println("Ready for next sale!");
            
        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            showAlert("Error", "Checkout failed: " + e.getMessage());
        } finally {
            DBConnection.closeResources(conn, pst, rs);
        }
    }
    
    private void printReceipt() {
        if (cartList.isEmpty()) {
            showAlert("Cart Empty", "No items to print in receipt");
            return;
        }
        
        StringBuilder receipt = new StringBuilder();
        receipt.append("================================\n");
        receipt.append("        COFFEE SHOP POS\n");
        receipt.append("================================\n");
        receipt.append(String.format("%-20s %6s %10s\n", "Item", "Qty", "Amount"));
        receipt.append("--------------------------------\n");
        
        for (Product item : cartList) {
            receipt.append(String.format("%-20s %6d %10.2f\n", 
                item.getName(), item.getStock(), item.getPrice() * item.getStock()));
        }
        
        receipt.append("--------------------------------\n");
        receipt.append(String.format("%-20s %16.2f\n", "Subtotal:", totalAmount));
        receipt.append(String.format("%-20s %16.2f\n", "Discount:", discountAmount));
        receipt.append(String.format("%-20s %16.2f\n", "TOTAL:", totalAmount - discountAmount));
        receipt.append("================================\n");
        receipt.append("        Thank You!\n");
        receipt.append("================================\n");
        
        // Print to console (in real app, send to printer)
        System.out.println(receipt.toString());
        
        // Optionally show in dialog
        TextArea receiptArea = new TextArea(receipt.toString());
        receiptArea.setEditable(false);
        receiptArea.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12; -fx-background-color: #000000; -fx-text-fill: #cccccc; -fx-control-inner-background: #000000;");
        
        Alert receiptAlert = new Alert(Alert.AlertType.INFORMATION);
        receiptAlert.setTitle("Receipt");
        receiptAlert.setHeaderText("Sale Receipt");
        receiptAlert.getDialogPane().setContent(receiptArea);
        // Apply dark mode styling
        DialogPane dialogPane = receiptAlert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #000000;");
        receiptAlert.showAndWait();
    }
    
    private void clearCart() {
        System.out.println("Clearing cart...");
        
        // Clear cart items (NOT product list!)
        cartList.clear();
        
        // Clear input fields
        productIdField.clear();
        quantityField.clear();
        discountField.clear();
        
        // Reset totals
        totalAmount = 0;
        discountAmount = 0;
        updateTotals();
        
        // Return focus to product ID field for next transaction
        productIdField.requestFocus();
        
        System.out.println("Cart cleared - ready for next transaction");
        System.out.println("Product list still has " + productList.size() + " products available");
    }
    
    private Product findProductByBarcodeOrId(String input) {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        Product product = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Try to find by barcode or ID
            String sql = "SELECT * FROM products WHERE (barcode = ? OR CAST(id AS TEXT) = ?)";
            pst = conn.prepareStatement(sql);
            pst.setString(1, input);
            pst.setString(2, input);
            rs = pst.executeQuery();
            
            if (rs.next()) {
                String barcode = null;
                try {
                    barcode = rs.getString("barcode");
                } catch (Exception e) {
                    barcode = null;
                }
                
                product = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getInt("stock"),
                    rs.getString("category"),
                    true,
                    barcode
                );
            }
            
        } catch (Exception e) {
            System.err.println("Error finding product: " + e.getMessage());
        } finally {
            DBConnection.closeResources(conn, pst, rs);
        }
        
        return product;
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        // Apply dark mode styling
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #000000;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #cccccc;");
        alert.showAndWait();
    }
    
    /**
     * Start the barcode receiver server for phone scanning
     */
    private void startBarcodeReceiver() {
        try {
            // Stop any existing receiver first (in case of re-login)
            if (barcodeReceiver != null && barcodeReceiver.isRunning()) {
                barcodeReceiver.stop();
            }
            
            // Create barcode receiver on port 8080 (will try alternative ports if needed)
            barcodeReceiver = new BarcodeReceiver(8080, this::handleReceivedBarcode);
            barcodeReceiver.start();
            
            // Show info dialog with server URL
            String serverURL = barcodeReceiver.getServerURL() + "/scanner";
            showAlert("Phone Scanner Ready", 
                "Barcode scanner server is running!\n\n" +
                "Open this URL on your phone:\n" +
                serverURL + "\n\n" +
                "Make sure your phone is on the same Wi-Fi network.\n\n" +
                "Note: If port 8080 was busy, the server may be using a different port.\n" +
                "Check the console for the actual port number.");
            
        } catch (Exception e) {
            System.err.println("Failed to start barcode receiver: " + e.getMessage());
            e.printStackTrace();
            showAlert("Scanner Error", 
                "Could not start barcode scanner server.\n" +
                "You can still use manual entry.\n\n" +
                "Possible causes:\n" +
                "- Port 8080 (and alternatives) are in use\n" +
                "- Firewall blocking the port\n" +
                "- Another instance of the app is running\n\n" +
                "Error: " + e.getMessage() + "\n\n" +
                "Try closing other applications or restarting the app.");
        }
    }
    
    /**
     * Handle barcode received from phone - OPTIMIZED VERSION
     * @param barcode The scanned barcode
     */
    private void handleReceivedBarcode(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            return;
        }
        
        try {
            // Quick lookup in memory first (fastest)
            Product foundProduct = findProductInList(barcode);
            
            // If not in memory, query database (single query, no reload)
            if (foundProduct == null) {
                foundProduct = findProductByBarcodeOrId(barcode);
                // If found in DB, add to list for future lookups (no full reload)
                if (foundProduct != null && !productList.contains(foundProduct)) {
                    productList.add(foundProduct);
                }
            }
            
            if (foundProduct != null) {
                // Quick validation checks
                if (!foundProduct.isAvailable()) {
                    showAlert("Product Unavailable", 
                        "Product '" + foundProduct.getName() + "' is currently unavailable.");
                    return;
                }
                
                if (foundProduct.getStock() <= 0) {
                    showAlert("Out of Stock", 
                        "Product '" + foundProduct.getName() + "' is out of stock.");
                    return;
                }
                
                // Directly add to cart with quantity 1 (skip addToCart overhead)
                addProductToCartDirectly(foundProduct, 1);
                
                // Non-blocking success feedback
                productIdField.setText(barcode);
            } else {
                // Product not found - fill field for manual entry
                productIdField.setText(barcode);
                showAlert("Product Not Found", 
                    "Barcode '" + barcode + "' not found.\nPlease check the barcode.");
            }
        } catch (Exception e) {
            System.err.println("Error processing barcode: " + e.getMessage());
            productIdField.setText(barcode); // Still fill field on error
        }
    }
    
    /**
     * Optimized method to add product directly to cart (bypasses addToCart overhead)
     */
    private void addProductToCartDirectly(Product product, int quantity) {
        if (product == null || quantity <= 0) return;
        
        // Check stock
        if (product.getStock() < quantity) {
            showAlert("Stock Error", "Insufficient stock. Available: " + product.getStock());
            return;
        }
        
        // Check if product already in cart
        boolean found = false;
        for (int i = 0; i < cartList.size(); i++) {
            Product cartItem = cartList.get(i);
            if (cartItem.getId() == product.getId()) {
                // Update quantity
                int newQuantity = cartItem.getStock() + quantity;
                if (newQuantity > product.getStock()) {
                    showAlert("Stock Error", "Total quantity exceeds available stock");
                    return;
                }
                // Replace the item with updated quantity
                Product updatedItem = new Product(cartItem.getId(), cartItem.getName(), 
                                     cartItem.getDescription(), cartItem.getPrice(),
                                     newQuantity, cartItem.getCategory(), true);
                cartList.set(i, updatedItem);
                cartTable.refresh();
                found = true;
                break;
            }
        }
        
        if (!found) {
            // Add new item to cart
            Product cartItem = new Product(product.getId(), product.getName(),
                                         product.getDescription(), product.getPrice(),
                                         quantity, product.getCategory(), true);
            cartList.add(cartItem);
        }
        
        updateTotals();
    }
    
    /**
     * Find product in the loaded product list by ID or barcode (optimized - memory only)
     */
    private Product findProductInList(String input) {
        if (input == null || input.trim().isEmpty() || productList == null) {
            return null;
        }
        
        // Optimized: single pass through list checking both ID and barcode
        for (Product product : productList) {
            // Check barcode first (most common case for scanning)
            if (product.getBarcode() != null && product.getBarcode().equals(input)) {
                return product;
            }
            // Check ID (try parse only if barcode didn't match)
            try {
                int productId = Integer.parseInt(input);
                if (product.getId() == productId) {
                    return product;
                }
            } catch (NumberFormatException e) {
                // Not a number, continue
            }
        }
        
        return null; // Not found in memory, caller will query DB if needed
    }
    
    /**
     * Stop the barcode receiver server (called on logout)
     */
    public void stopBarcodeReceiver() {
        if (barcodeReceiver != null) {
            barcodeReceiver.stop();
            barcodeReceiver = null;
        }
    }
}
